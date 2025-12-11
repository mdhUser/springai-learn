package org.maxwell.springailearn.trigger.http;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.maxwell.springailearn.api.IRagService;
import org.maxwell.springailearn.api.response.Response;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.core.io.PathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/rag/")
public class RagController implements IRagService {

    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private PgVectorStore pgVectorStore;
    @Resource
    private RedissonClient redissonClient;

    @Override
    @GetMapping(value = "query_rag_tag_list")
    public Response<List<String>> queryRagTagList() {
        RList<String> ragList = redissonClient.getList("ragList");
        return Response.<List<String>>builder()
                .data(new ArrayList<>(ragList))
                .code("200")
                .info("成功")
                .build();
    }

    @Override
    @PostMapping(value = "upload_file", headers = "content-type=multipart/form-data")
    public Response<String> uploadFile(@RequestParam("ragTag") String ragTag, @RequestParam("file") List<MultipartFile> files) {
        log.info("上传文件，ragTag:{}", ragTag);
        for (MultipartFile file : files) {
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = tikaDocumentReader.get();
            List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

            // 添加知识库标签
            documents.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));
            documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", ragTag));


            pgVectorStore.accept(documentSplitterList);

            RList<String> ragList = redissonClient.getList("ragList");
            if (!ragList.contains(ragTag)) {
                ragList.add(ragTag);
            }
        }
        log.info("上传知识库完成 {}", ragTag);
        return Response.<String>builder().code("0000").info("调用成功").build();
    }


    @Override
    @PostMapping(value = "analyze_git_repository")
    public Response<String> analyzeGitRepository(String repoUrl, String userName, String token) throws Exception {
        String localPath = "./git-cloned-repo";
        String repoProjectName = extractProjectName(repoUrl);
        log.info("克隆路径：{}", new File(localPath).getAbsolutePath());

        FileUtils.deleteDirectory(new File(localPath));

        Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(new File(localPath))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, token))
                .call();

        Files.walkFileTree(Paths.get(localPath), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                log.info("{} 遍历解析路径，上传知识库:{}", repoProjectName, file.getFileName());
                try {
                    TikaDocumentReader reader = new TikaDocumentReader(new PathResource(file));
                    List<Document> documents = reader.get();
                    List<Document> documentSplitterList = tokenTextSplitter.apply(documents);

                    documents.forEach(doc -> doc.getMetadata().put("knowledge", repoProjectName));

                    documentSplitterList.forEach(doc -> doc.getMetadata().put("knowledge", repoProjectName));

                    pgVectorStore.accept(documentSplitterList);
                } catch (Exception e) {
                    log.error("遍历解析路径，上传知识库失败:{}", file.getFileName());
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                log.info("Failed to access file: {} - {}", file.toString(), exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });

        FileUtils.deleteDirectory(new File(localPath));

        RList<String> elements = redissonClient.getList("ragTag");
        if (!elements.contains(repoProjectName)) {
            elements.add(repoProjectName);
        }

        git.close();

        log.info("遍历解析路径，上传完成:{}", repoUrl);

        return Response.<String>builder().code("0000").info("调用成功").build();
    }


    private String extractProjectName(String repoUrl) {
        String[] parts = repoUrl.split("/");
        String projectNameWithGit = parts[parts.length - 1];
        return projectNameWithGit.replace(".git", "");
    }

}
