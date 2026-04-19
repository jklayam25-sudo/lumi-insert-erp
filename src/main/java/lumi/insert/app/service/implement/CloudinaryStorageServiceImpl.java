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

/**
 * Implementation of {@link StorageService} using Cloudinary for cloud-based media management.
 * <p>
 * This service provides methods to upload images from local paths or byte arrays,
 * and to delete assets using their public identifiers.
 * </p>
 * * @author KelvinKhodes
 * @since 1.0.0
 */
@Service
@Slf4j
public class CloudinaryStorageServiceImpl implements StorageService{

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    ObjectMapper objectMapper;
 
    /**
     * Uploads an image file from a specific system path. 
     * * @param path       the local {@link Path} of the file to be uploaded.
     * @param folderName the destination folder name in Cloudinary.
     * @return a {@link CloudinaryResponse} containing asset metadata and URLs.
     * @throws IOException if the file cannot be read or the upload fails.
     */
    @Override
    public CloudinaryResponse uploadImage(Path path, String folderName) throws IOException {
        log.info("Uploading image from path={} to folder={}", path, folderName);

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
            CloudinaryResponse response = objectMapper.convertValue(rawResponse, CloudinaryResponse.class);
            log.info("Uploaded image publicId={} secureUrl={}", response.getPublicId(), response.getSecureUrl());
            return response;
        } catch (IOException e) {
            log.error("Upload failed for path={} folder={} message={}", path, folderName, e.getMessage(), e);
            throw e;
        }

    }
 
    /**
     * Uploads image data provided as a byte array.
     * <p>This method performs a synchronous upload directly from memory buffer.</p>
     * * @param files      the raw byte array of the image.
     * @param fileName   the desired public ID/filename for the asset.
     * @param folderName the destination folder name in Cloudinary.
     * @return a {@link CloudinaryResponse} containing asset metadata.
     * @throws IOException if the upload process encounters a network or API error.
     */
    @Override
    public CloudinaryResponse uploadImageSync(byte[] files, String fileName, String folderName) throws IOException {
        log.info("Uploading image bytes with publicId={} to folder={}", fileName, folderName);
         try  { 
            Map<?, ?> uploadOptions = ObjectUtils.asMap(   
                "overwrite", true,                  
                "resource_type", "image",
                "public_id", fileName,
                "filename", fileName,
                "folder", folderName
            ); 
            Map<?, ?> rawResponse = cloudinary.uploader().upload(files, uploadOptions); 
            CloudinaryResponse response = objectMapper.convertValue(rawResponse, CloudinaryResponse.class);
            log.info("Uploaded image publicId={} secureUrl={}", response.getPublicId(), response.getSecureUrl());
            return response;
        } catch (IOException e) {
            log.error("Upload failed for publicId={} folder={} message={}", fileName, folderName, e.getMessage(), e);
            throw e;
        }

    }

    /**
     * Deletes an asset from Cloudinary storage.
     * * @param publicId the unique identifier of the asset (including folder path if applicable).
     * @return {@code true} if the deletion was successful ("ok"), {@code false} otherwise.
     */
    @Override
    public Boolean deleteImage(String publicId) {
        log.info("Deleting image with publicId={}", publicId);
        try {
            Map<?, ?> destroy = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            boolean success = String.valueOf(destroy.get("result")).equals("ok");
            log.info("Delete image publicId={} success={}", publicId, success);
            return success; 
        } catch (IOException e) {
            log.error("Failed to destroy image publicId={} message={}", publicId, e.getMessage(), e);
            return false;
        }
    }
 
    
}
