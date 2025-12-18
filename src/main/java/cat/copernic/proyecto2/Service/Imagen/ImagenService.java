package cat.copernic.proyecto2.Service.Imagen;

import org.springframework.web.multipart.MultipartFile;

public interface ImagenService {
    String guardarImagen(MultipartFile archivo, String subCarpeta);
    void eliminarImagen(String nombreImagen, String subCarpeta);
}
