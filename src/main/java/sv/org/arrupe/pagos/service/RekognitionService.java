package sv.org.arrupe.pagos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.util.List;

@Service
public class RekognitionService {

    private final RekognitionClient rekognitionClient;
    private final String collectionId = "MarketCupFaces"; // Nombre de nuestra colección de rostros

    // Umbrales de calidad
    private static final float MIN_BRIGHTNESS = 40.0f;
    private static final float MIN_SHARPNESS = 50.0f;
    private static final float FACE_MATCH_THRESHOLD = 90.0f;


    public RekognitionService(
            @Value("${aws.accessKeyId}") String accessKey,
            @Value("${aws.secretKey}") String secretKey,
            @Value("${aws.region}") String region) {
        
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.rekognitionClient = RekognitionClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        // Asegurarse de que la colección exista al iniciar
        try {
            CreateCollectionRequest request = CreateCollectionRequest.builder()
                .collectionId(collectionId)
                .build();
            rekognitionClient.createCollection(request);
            System.out.println("Colección de rostros creada: " + collectionId);
        } catch (ResourceAlreadyExistsException e) {
            System.out.println("La colección de rostros ya existe: " + collectionId);
        }
    }

    /**
     * NUEVO: Verifica la calidad de una imagen antes de registrarla.
     * @param imageBytes Los bytes de la imagen a analizar.
     * @return true si la imagen cumple con los umbrales de calidad, false en caso contrario.
     */
    public boolean checkImageQuality(byte[] imageBytes) {
        try {
            Image image = Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build();
            DetectFacesRequest detectFacesRequest = DetectFacesRequest.builder()
                    .image(image)
                    .attributes(Attribute.ALL)
                    .build();

            DetectFacesResponse detectFacesResponse = rekognitionClient.detectFaces(detectFacesRequest);
            if (detectFacesResponse.faceDetails().isEmpty()) {
                System.out.println("Calidad de imagen rechazada: No se detectó ningún rostro.");
                return false; // No se detectaron rostros
            }

            FaceDetail faceDetail = detectFacesResponse.faceDetails().get(0);
            float brightness = faceDetail.quality().brightness();
            float sharpness = faceDetail.quality().sharpness();
            
            System.out.println("Calidad de imagen - Brillo: " + brightness + ", Nitidez: " + sharpness);

            if (brightness < MIN_BRIGHTNESS || sharpness < MIN_SHARPNESS) {
                System.out.println("Calidad de imagen rechazada: Brillo o nitidez insuficientes.");
                return false;
            }
            return true;
        } catch (RekognitionException e) {
            System.err.println("Error en Rekognition al verificar la calidad de la imagen: " + e.awsErrorDetails().errorMessage());
            return false;
        }
    }


    /**
     * Registra un rostro en la colección y devuelve el FaceId.
     */
    public String indexFace(byte[] imageBytes) {
        Image image = Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build();

        IndexFacesRequest request = IndexFacesRequest.builder()
                .collectionId(collectionId)
                .image(image)
                .maxFaces(1) // Solo indexar el rostro más grande
                .detectionAttributes(Attribute.DEFAULT)
                .build();

        IndexFacesResponse response = rekognitionClient.indexFaces(request);
        
        if (response.hasFaceRecords() && !response.faceRecords().isEmpty()) {
            return response.faceRecords().get(0).face().faceId();
        }
        throw new RuntimeException("No se detectó ningún rostro en la imagen proporcionada para indexar.");
    }

    /**
     * Busca un rostro en la colección y devuelve el FaceId del mejor resultado.
     */
    public String searchFaceByImage(byte[] imageBytes) {
        Image image = Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build();
        
        SearchFacesByImageRequest request = SearchFacesByImageRequest.builder()
                .collectionId(collectionId)
                .image(image)
                .faceMatchThreshold(FACE_MATCH_THRESHOLD) // Umbral de confianza
                .maxFaces(1)
                .build();
                
        SearchFacesByImageResponse response = rekognitionClient.searchFacesByImage(request);

        if (response.hasFaceMatches() && !response.faceMatches().isEmpty()) {
            // Devuelve el FaceId de la coincidencia más cercana
            return response.faceMatches().get(0).face().faceId();
        }
        return null; // No se encontró ninguna coincidencia
    }
}