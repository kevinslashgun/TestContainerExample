package my.wikicasa.web.repository;

import my.wikicasa.web.entity.RealEstate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Repository
public class RealEstateRepository {

    private final JdbcTemplate jdbcTemplate;

    public RealEstateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Mapper per convertire le righe del risultato in oggetti RealEstate
    private final RowMapper<RealEstate> realEstateRowMapper = (rs, rowNum) -> {
        RealEstate realEstate = new RealEstate();
        realEstate.setId(rs.getLong("id"));
        realEstate.setName(rs.getString("name"));
        realEstate.setAddress(rs.getString("address"));
        realEstate.setPrice(rs.getDouble("price"));
        realEstate.setRooms(rs.getInt("rooms"));
        realEstate.setBathrooms(rs.getInt("bathrooms"));
        realEstate.setSqMeters(rs.getDouble("sq_meters"));
        return realEstate;
    };

    // Creare un nuovo RealEstate
    public RealEstate save(RealEstate realEstate) {
        String sql = "INSERT INTO real_estate (name, address, price, rooms, bathrooms, sq_meters) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try {
            Long id = jdbcTemplate.queryForObject(sql, Long.class, realEstate.getName(), realEstate.getAddress(),
                    realEstate.getPrice(), realEstate.getRooms(), realEstate.getBathrooms(), realEstate.getSqMeters());
            realEstate.setId(id);
            return realEstate;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "real estate already exists", e);
        }
    }

    // Ottenere una lista di tutti i RealEstate
    public List<RealEstate> findAll() {
        String sql = "SELECT * FROM real_estate";
        return jdbcTemplate.query(sql, realEstateRowMapper);
    }

    // Ottenere un RealEstate per ID
    public Optional<RealEstate> findById(Long id) {
        String sql = "SELECT * FROM real_estate WHERE id = ?";
        return jdbcTemplate.query(sql, realEstateRowMapper, id).stream().findFirst();
    }

    // Aggiornare un RealEstate esistente
    public RealEstate update(RealEstate realEstate) {
        String sql = "UPDATE real_estate SET name = ?, address = ?, price = ?, rooms = ?, bathrooms = ?, sq_meters = ? WHERE id = ?";
        jdbcTemplate.update(sql, realEstate.getName(), realEstate.getAddress(), realEstate.getPrice(),
                realEstate.getRooms(), realEstate.getBathrooms(), realEstate.getSqMeters(), realEstate.getId());
        return realEstate;
    }

    // Cancellare un RealEstate per ID
    public void deleteById(Long id) {
        String sql = "DELETE FROM real_estate WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
