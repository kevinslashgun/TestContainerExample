package my.wikicasa.web.service;

import my.wikicasa.web.entity.RealEstate;
import my.wikicasa.web.exception.RealEstateNotFoundException;
import my.wikicasa.web.repository.RealEstateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public RealEstate updateRealEstate(Long id, RealEstate updatedRealEstate) {
        realEstateRepository.findById(id).orElseThrow(() -> new RealEstateNotFoundException(id));
        updatedRealEstate.setId(id);
        return realEstateRepository.update(updatedRealEstate);
    }

    public void deleteRealEstate(Long id) {
        realEstateRepository.deleteById(id);
    }
}
