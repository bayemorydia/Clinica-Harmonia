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
            // Carpeta base de tu proyecto
            String folder = "src/main/resources/static/images/";
            byte[] bytes = file.getBytes();
            // Creamos un nombre único para evitar colisiones
            String nombreImagen = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(folder + nombreImagen);
            
            // Crea los directorios si no existen
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);

            return nombreImagen;
        } catch (IOException e) {
            System.err.println("Error al guardar imagen: " + e.getMessage());
            return "default-user.png";
        }
    }

    @Override
    public void eliminarImagen(String nombreImagen, String carpeta) {
        if (nombreImagen == null || nombreImagen.equals("default-user.png") || nombreImagen.isEmpty()) {
            return;
        }
        try {
            String folder = "src/main/resources/static/images/";
            Path path = Paths.get(folder + nombreImagen);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Error al eliminar imagen: " + e.getMessage());
        }
    }
}