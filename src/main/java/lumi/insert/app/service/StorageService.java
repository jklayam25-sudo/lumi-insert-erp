package lumi.insert.app.service;

import java.io.IOException;
import java.nio.file.Path; 

import lumi.insert.app.core.entity.nondatabase.CloudinaryResponse;

public interface StorageService {

    CloudinaryResponse uploadImage(Path path, String folderName) throws IOException; 

    CloudinaryResponse uploadImageSync(byte[] bytes, String fileName, String folderName) throws IOException; 

    Boolean deleteImage(String publicId); 

}
