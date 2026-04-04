package lumi.insert.app.service.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException; 
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map; 

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader; 

import lumi.insert.app.core.entity.nondatabase.CloudinaryResponse;
import lumi.insert.app.service.implement.CloudinaryStorageServiceImpl;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {
    
    @InjectMocks
    CloudinaryStorageServiceImpl storageServiceImpl;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    Cloudinary cloudinary;

    @Mock
    Uploader uploader;

    @Test
    void uploadImage_shouldReturnCloudinaryResponse() throws IOException{
        Map<?, ?> response = Map.of("public_id", "id123", "secure_url", "www.test", "format", "image/png");
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("test", "_upload");  
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(), any())).thenReturn(response); 
            CloudinaryResponse uploadImage = storageServiceImpl.uploadImage(tempFile, "testFolder");
            verify(uploader, times(1)).upload(argThat(arg -> ((byte[]) arg).length == 0), argThat(arg -> ((Map<?, ?>) arg).get("folder").equals("testFolder")));
            assertEquals(response.get("secure_url"), uploadImage.getSecureUrl());    
        } catch (Exception e) {
           throw e;
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void uploadImageSync_shouldReturnCloudinaryResponse() throws IOException{
        Map<?, ?> response = Map.of("public_id", "id123", "secure_url", "www.test", "format", "image/png");
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("test", "_upload");  
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(), any())).thenReturn(response); 

            CloudinaryResponse uploadImage = storageServiceImpl.uploadImageSync(Files.readAllBytes(tempFile), "testFile", "testFolder");
            verify(uploader, times(1)).upload(argThat(arg -> ((byte[]) arg).length == 0), argThat(arg -> ((Map<?, ?>) arg).get("folder").equals("testFolder")));
            assertEquals(response.get("secure_url"), uploadImage.getSecureUrl());    
        } catch (Exception e) {
           throw e;
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void deleteImage_shouldReturnTrue() throws IOException{
        Map<?, ?> response = Map.of("result", "ok");  
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(any(), any())).thenReturn(response); 
            
            assertTrue(storageServiceImpl.deleteImage("publicTest"));
            verify(uploader, times(1)).destroy(eq("publicTest"), any());
    }

}
