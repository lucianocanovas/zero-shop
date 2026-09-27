package ingsoftware.zeroshop.service.actor;

import ingsoftware.zeroshop.dto.SupplierFormDTO;
import ingsoftware.zeroshop.entity.actor.ContactEmail;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.enums.ContactType;
import ingsoftware.zeroshop.enums.PhoneType;
import ingsoftware.zeroshop.repository.actor.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

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
        if (dto.getAddress() != null && !dto.getAddress().isEmpty()) {
            Address address = new Address();
            address.setStreet(dto.getAddress());
            address.setZipCode("N/A"); // Default value as it's required
            address.setNumber("S/N"); // Default value as it's required
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
        if (dto.getAddress() != null && !dto.getAddress().isEmpty()) {
            Address address = new Address();
            address.setStreet(dto.getAddress());
            address.setZipCode("N/A");
            address.setNumber("S/N");
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
