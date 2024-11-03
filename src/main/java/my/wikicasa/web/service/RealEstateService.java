package my.wikicasa.web.service;

import my.wikicasa.web.entity.RealEstate;
import my.wikicasa.web.exception.RealEstateNotFoundException;
import my.wikicasa.web.repository.RealEstateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RealEstateService {

    private final RealEstateRepository realEstateRepository;

    @Autowired
    public RealEstateService(RealEstateRepository realEstateRepository) {
        this.realEstateRepository = realEstateRepository;
    }

    public RealEstate createRealEstate(RealEstate realEstate) {
        return realEstateRepository.save(realEstate);
    }

    public List<RealEstate> getAllRealEstates() {
        return realEstateRepository.findAll();
    }

    public RealEstate getRealEstateById(Long id) {
        return realEstateRepository.findById(id).orElseThrow(() -> new RealEstateNotFoundException(id));
    }

    public RealEstate updateRealEstate(Long id, Map<String, Object> updates) {
        RealEstate realEstate = realEstateRepository.findById(id).orElseThrow(() -> new RealEstateNotFoundException(id));
        updates.forEach((key, value) -> {
            switch (key) {
                case "name" -> realEstate.setName((String) value);
                case "address" -> realEstate.setAddress((String) value);
                case "price" -> realEstate.setPrice(Double.valueOf(value.toString()));
                case "rooms" -> realEstate.setRooms(Integer.valueOf(value.toString()));
                case "bathrooms" -> realEstate.setBathrooms(Integer.valueOf(value.toString()));
                case "sqMeters" -> realEstate.setSqMeters(Double.valueOf(value.toString()));
            }
        });
        return realEstateRepository.update(realEstate);
    }

    public void deleteRealEstate(Long id) {
        realEstateRepository.deleteById(id);
    }
}
