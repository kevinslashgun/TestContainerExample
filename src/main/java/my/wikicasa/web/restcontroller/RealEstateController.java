package my.wikicasa.web.restcontroller;

import jakarta.validation.Valid;
import my.wikicasa.web.entity.RealEstate;
import my.wikicasa.web.service.RealEstateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/realestate")
public class RealEstateController {

    private final RealEstateService realEstateService;

    public RealEstateController(RealEstateService realEstateService) {
        this.realEstateService = realEstateService;
    }

    @PostMapping
    public ResponseEntity<RealEstate> createRealEstate(@Valid @RequestBody RealEstate realEstate) {
        RealEstate createdRealEstate = realEstateService.createRealEstate(realEstate);
        return new ResponseEntity<>(createdRealEstate, HttpStatus.CREATED);
    }

    @GetMapping
    public List<RealEstate> getAllRealEstates() {
        return realEstateService.getAllRealEstates();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RealEstate> getRealEstateById(@PathVariable Long id) {
        RealEstate realEstate = realEstateService.getRealEstateById(id);
        return ResponseEntity.ok(realEstate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RealEstate> updateRealEstate(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        RealEstate realEstate = realEstateService.updateRealEstate(id, updates);
        return ResponseEntity.ok(realEstate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRealEstate(@PathVariable Long id) {
        realEstateService.deleteRealEstate(id);
        return ResponseEntity.noContent().build();
    }

}
