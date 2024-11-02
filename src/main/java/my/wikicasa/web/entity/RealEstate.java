package my.wikicasa.web.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealEstate {

    @JsonIgnore
    private Long id;

    @NotBlank(message = "The name cannot be blank")
    private String name;

    @NotBlank(message = "The address cannot be blank")
    private String address;

    @NotNull(message = "The price is required")
    @Positive(message = "The price must be positive")
    private Double price;

    @NotNull(message = "The number of rooms is required")
    @Positive(message = "The number of rooms must be positive")
    private Integer rooms;

    @NotNull(message = "The number of bathrooms is required")
    @Positive(message = "The number of bathrooms must be positive")
    private Integer bathrooms;

    @NotNull(message = "The square meters are required")
    @Positive(message = "The square meters must be positive")
    private Double sqMeters;

    public RealEstate(String name, String address, Double price, Integer rooms, Integer bathrooms, Double sqMeters) {
        this.name = name;
        this.address = address;
        this.price = price;
        this.rooms = rooms;
        this.bathrooms = bathrooms;
        this.sqMeters = sqMeters;
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(List<RealEstate> realEstates) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(realEstates);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
