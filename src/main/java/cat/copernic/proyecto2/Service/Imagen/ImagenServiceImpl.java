package cat.copernic.proyecto2.Service.Imagen;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImagenServiceImpl implements ImagenService {

    @Override
    public String guardarImagen(MultipartFile file, String carpeta) {
        if (file == null || file.isEmpty()) {
            return "default-user.png";
        }
        try {
            // Ruta base donde se guardarán las imágenes dentro del proyecto
            String folder = "src/main/resources/static/images/";
            // Convertimos el archivo a bytes para poder guardarlo en disco
            byte[] bytes = file.getBytes();
            // Creamos un nombre único para evitar colisiones
            String nombreImagen = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(folder + nombreImagen);
            // Crea los directorios si no existen
            Files.createDirectories(path.getParent());
            // Escribimos el archivo en la ruta especificada
            Files.write(path, bytes);
            return nombreImagen;
        } catch (IOException e) {
            System.err.println("Error al guardar imagen: " + e.getMessage());
            return "default-user.png";
        }
    }

    @Override
    public void eliminarImagen(String nombreImagen, String carpeta) {
        // Si no hay nombre de imagen o es la imagen por defecto, no se elimina nada
        if (nombreImagen == null || nombreImagen.equals("default-user.png") || nombreImagen.isEmpty()) {
            return;
        }
        try {
            String folder = "src/main/resources/static/images/";
            // Ruta completa del archivo a eliminar
            Path path = Paths.get(folder + nombreImagen);
            // Elimina el archivo si existe
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Error al eliminar imagen: " + e.getMessage());
        }
    }
}