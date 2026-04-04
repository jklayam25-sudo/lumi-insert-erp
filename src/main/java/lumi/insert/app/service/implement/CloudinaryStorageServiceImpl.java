package lumi.insert.app.service.implement;
 
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import tools.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import lumi.insert.app.core.entity.nondatabase.CloudinaryResponse;
import lumi.insert.app.service.StorageService;

@Service
@Slf4j
public class CloudinaryStorageServiceImpl implements StorageService{

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    ObjectMapper objectMapper;
 
    @Override
    public CloudinaryResponse uploadImage(Path path, String folderName) throws IOException {

        try (InputStream inputStream = Files.newInputStream(path)) {
            String fileName = path.getFileName().toString();
            String realName = fileName.split("_upload")[0]; 

            Map<?, ?> uploadOptions = ObjectUtils.asMap(   
                "overwrite", true,                  
                "resource_type", "image",
                "public_id", realName,
                "filename", realName,
                "folder", folderName
            ); 

            Map<?, ?> rawResponse = cloudinary.uploader().upload(inputStream.readAllBytes(), uploadOptions); 
            return objectMapper.convertValue(rawResponse, CloudinaryResponse.class);
        } catch (IOException e) {
            log.error("Upload failed, message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }
 
    @Override
    public CloudinaryResponse uploadImageSync(byte[] files, String fileName, String folderName) throws IOException {
        
         try  { 
            Map<?, ?> uploadOptions = ObjectUtils.asMap(   
                "overwrite", true,                  
                "resource_type", "image",
                "public_id", fileName,
                "filename", fileName,
                "folder", folderName
            ); 
            Map<?, ?> rawResponse = cloudinary.uploader().upload(files, uploadOptions); 
            return objectMapper.convertValue(rawResponse, CloudinaryResponse.class);
        } catch (IOException e) {
            log.error("Upload failed, message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

    }

    @Override
    public Boolean deleteImage(String publicId) {
        try {
            Map<?, ?> destroy = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return String.valueOf(destroy.get("result")).equals("ok"); 
        } catch (IOException e) {
            log.error("Fail to destroy image, messages: " + e.getMessage());
            return false;
        }
    }
 
    
}
