package cat.copernic.proyecto2.Service.Imagen;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImagenServiceImpl implements ImagenService {

    @Override
    public String guardarImagen(MultipartFile archivo, String subCarpeta) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        try {
            String directorioRecursos = System.getProperty("user.dir") + "/src/main/resources/static/Images/" + subCarpeta;
            Path rutaAbsoluta = Paths.get(directorioRecursos);
            if (!Files.exists(rutaAbsoluta)) {
                Files.createDirectories(rutaAbsoluta);
            }
            String nombreOriginal = archivo.getOriginalFilename();
            String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            String nombreUnico = UUID.randomUUID().toString() + extension;
            Path rutaArchivo = rutaAbsoluta.resolve(nombreUnico);
            Files.write(rutaArchivo, archivo.getBytes());
            return nombreUnico;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void eliminarImagen(String nombreImagen, String subCarpeta) {
        if (nombreImagen == null || nombreImagen.equals("default-user.png")) return;        
        try {
            String directorio = System.getProperty("user.dir") + "/src/main/resources/static/Images/" + subCarpeta;
            Path rutaArchivo = Paths.get(directorio).resolve(nombreImagen);
            Files.deleteIfExists(rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error eliminando archivo: " + e.getMessage());
        }
    }
}