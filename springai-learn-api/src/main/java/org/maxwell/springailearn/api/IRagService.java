package org.maxwell.springailearn.api;


import org.maxwell.springailearn.api.response.Response;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

public interface IRagService {

    Response<List<String>> queryRagTagList();

    Response<String> uploadFile(String ragTag, List<MultipartFile> files);

    Response<String> analyzeGitRepository(String repoUrl, String userName, String token) throws Exception;

}
