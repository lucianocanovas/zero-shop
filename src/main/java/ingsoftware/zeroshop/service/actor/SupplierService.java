package ingsoftware.zeroshop.service.actor;

import ingsoftware.zeroshop.dto.SupplierFormDTO;
import ingsoftware.zeroshop.entity.actor.ContactEmail;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.enums.ContactType;
import ingsoftware.zeroshop.enums.PhoneType;
import ingsoftware.zeroshop.repository.actor.SupplierRepository;
import ingsoftware.zeroshop.repository.location.CityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CityRepository cityRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAllByDeletedFalse();
    }

    public Supplier getSupplierById(UUID id) {
        return supplierRepository.findActive(id).orElse(null);
    }

    public Supplier createSupplier(SupplierFormDTO dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setCuit(dto.getCuit());

        supplier.setContact(new java.util.ArrayList<>());
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            ContactEmail email = new ContactEmail();
            email.setContactType(ContactType.WORK);
            email.setEmail(dto.getEmail()); // Set specific email field
            email.setObservation("Email corporativo");
            supplier.getContact().add(email);
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            ContactPhone phone = new ContactPhone();
            phone.setContactType(ContactType.WORK);
            phone.setPhoneType(PhoneType.MOBILE); // Required
            phone.setPhoneNumber(dto.getPhone()); // Required
            phone.setObservation("Teléfono corporativo");
            supplier.getContact().add(phone);
        }

        supplier.setAddress(new java.util.ArrayList<>());
        if (dto.getAddress() != null) {
            City city = cityRepository.findById(dto.getAddress().getCityId())
                    .orElseThrow(() -> new EntityNotFoundException("Ciudad no encontrada con ID: " + dto.getAddress().getCityId()));

            Address address = new Address();
            address.setStreet(dto.getAddress().getStreet());
            address.setNumber(dto.getAddress().getNumber());
            address.setZipCode(dto.getAddress().getZipCode());
            address.setFloor(dto.getAddress().getFloor());
            address.setApartment(dto.getAddress().getApartment());
            address.setObservations(dto.getAddress().getObservations());
            address.setCity(city);
            supplier.getAddress().add(address);
        }

        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(UUID id, SupplierFormDTO dto) {
        Supplier supplier = getSupplierById(id);
        if (supplier == null)
            return null;

        supplier.setName(dto.getName());
        supplier.setCuit(dto.getCuit());

        // Simple approach: clear and re-add for this demo
        supplier.getContact().clear();
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
            ContactEmail email = new ContactEmail();
            email.setContactType(ContactType.WORK);
            email.setEmail(dto.getEmail()); // Set specific email field
            email.setObservation("Email corporativo");
            supplier.getContact().add(email);
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            ContactPhone phone = new ContactPhone();
            phone.setContactType(ContactType.WORK);
            phone.setPhoneType(PhoneType.MOBILE); // Required
            phone.setPhoneNumber(dto.getPhone()); // Required
            phone.setObservation("Teléfono corporativo");
            supplier.getContact().add(phone);
        }

        supplier.getAddress().clear();
        if (dto.getAddress() != null) {
            City city = cityRepository.findById(dto.getAddress().getCityId())
                    .orElseThrow(() -> new EntityNotFoundException("Ciudad no encontrada con ID: " + dto.getAddress().getCityId()));

            Address address = new Address();
            address.setStreet(dto.getAddress().getStreet());
            address.setNumber(dto.getAddress().getNumber());
            address.setZipCode(dto.getAddress().getZipCode());
            address.setFloor(dto.getAddress().getFloor());
            address.setApartment(dto.getAddress().getApartment());
            address.setObservations(dto.getAddress().getObservations());
            address.setCity(city);
            supplier.getAddress().add(address);
        }

        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(UUID id) {
        Supplier supplier = getSupplierById(id);
        if (supplier != null) {
            supplier.setDeleted(true);
            supplierRepository.save(supplier);
        }
    }
}
