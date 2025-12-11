package org.maxwell.springailearn.trigger.http;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.maxwell.springailearn.api.IRagService;
import org.maxwell.springailearn.api.response.Response;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping(value = "upload_file")
    public Response<String> uploadFile(String ragTag, List<MultipartFile> files) {
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
    public Response<String> analyzeGitRepository(String repoUrl, String userName, String token) throws Exception {


        return null;
    }

}
