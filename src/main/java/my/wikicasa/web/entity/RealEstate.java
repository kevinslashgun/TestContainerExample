package my.wikicasa.web.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor // Genera un costruttore senza argomenti
@AllArgsConstructor // Genera un costruttore con tutti i parametri
public class RealEstate {

    private Long id;
    private String name;
    private String address;
    private BigDecimal price;
    private Integer rooms;
    private Integer bathrooms;
    private Double sqMeters;

    // Il metodo toString() viene generato automaticamente da Lombok
}
