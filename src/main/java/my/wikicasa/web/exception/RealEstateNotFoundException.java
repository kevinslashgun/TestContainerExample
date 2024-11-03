package my.wikicasa.web.exception;

public class RealEstateNotFoundException extends RuntimeException {
    public RealEstateNotFoundException(Long id) {
        super("RealEstate with ID: " + id + " not found");
    }
}
