package ingsoftware.zeroshop.service.actor;

import ingsoftware.zeroshop.dto.SupplierFormDTO;
import ingsoftware.zeroshop.entity.actor.Contact;
import ingsoftware.zeroshop.entity.actor.ContactEmail;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.enums.ContactType;
import ingsoftware.zeroshop.enums.PhoneType;
import ingsoftware.zeroshop.repository.actor.SupplierRepository;
import ingsoftware.zeroshop.repository.location.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        if (dto.getAddress() != null && dto.getAddress().getCityId() != null) {
            City city = cityRepository.findById(dto.getAddress().getCityId()).orElse(null);
            if (city != null) {
                Address address = new Address();
                address.setStreet(dto.getAddress().getStreet() != null ? dto.getAddress().getStreet() : "");
                address.setNumber(dto.getAddress().getNumber() != null ? dto.getAddress().getNumber() : "");
                address.setZipCode(dto.getAddress().getZipCode() != null ? dto.getAddress().getZipCode() : "5500");
                address.setFloor(dto.getAddress().getFloor());
                address.setApartment(dto.getAddress().getApartment());
                address.setObservations(dto.getAddress().getObservations());
                address.setCity(city);
                supplier.getAddress().add(address);
            }
        }

        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(UUID id, SupplierFormDTO dto) {
        Supplier supplier = getSupplierById(id);
        if (supplier == null)
            return null;

        supplier.setName(dto.getName());
        supplier.setCuit(dto.getCuit());

        if (supplier.getContact() == null) {
            supplier.setContact(new java.util.ArrayList<>());
        }
        if (supplier.getAddress() == null) {
            supplier.setAddress(new java.util.ArrayList<>());
        }

        // Si el proveedor no tenía contactos previos y el formulario incluye uno
        if (supplier.getContact().isEmpty()) {
            if (dto.getEmail() != null && !dto.getEmail().isEmpty()) {
                ContactEmail email = new ContactEmail();
                email.setContactType(ContactType.WORK);
                email.setEmail(dto.getEmail());
                email.setObservation("Email corporativo");
                supplier.getContact().add(email);
            }
            if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
                ContactPhone phone = new ContactPhone();
                phone.setContactType(ContactType.WORK);
                phone.setPhoneType(PhoneType.MOBILE);
                phone.setPhoneNumber(dto.getPhone());
                phone.setObservation("Teléfono corporativo");
                supplier.getContact().add(phone);
            }
        }

        // Si el proveedor no tenía dirección previa y el formulario incluye una
        if (supplier.getAddress().isEmpty() && dto.getAddress() != null && dto.getAddress().getCityId() != null) {
            City city = cityRepository.findById(dto.getAddress().getCityId()).orElse(null);
            if (city != null) {
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
        }

        return supplierRepository.save(supplier);
    }

    public void addAddressToSupplier(UUID supplierId, Address address) {
        Supplier supplier = getSupplierById(supplierId);
        if (supplier != null) {
            if (supplier.getAddress() == null) {
                supplier.setAddress(new java.util.ArrayList<>());
            }
            supplier.getAddress().add(address);
            supplierRepository.save(supplier);
        }
    }

    public void removeAddressFromSupplier(UUID supplierId, UUID addressId) {
        Supplier supplier = getSupplierById(supplierId);
        if (supplier != null && supplier.getAddress() != null) {
            for (Address a : supplier.getAddress()) {
                if (a.getId() != null && a.getId().equals(addressId)) {
                    a.setDeleted(true);
                    break;
                }
            }
            supplierRepository.save(supplier);
        }
    }

    public void addContactToSupplier(UUID supplierId, Contact contact) {
        Supplier supplier = getSupplierById(supplierId);
        if (supplier != null) {
            if (supplier.getContact() == null) {
                supplier.setContact(new java.util.ArrayList<>());
            }
            supplier.getContact().add(contact);
            supplierRepository.save(supplier);
        }
    }

    public void removeContactFromSupplier(UUID supplierId, UUID contactId) {
        Supplier supplier = getSupplierById(supplierId);
        if (supplier != null && supplier.getContact() != null) {
            for (Contact c : supplier.getContact()) {
                if (c.getId() != null && c.getId().equals(contactId)) {
                    c.setDeleted(true);
                    break;
                }
            }
            supplierRepository.save(supplier);
        }
    }

    public void deleteSupplier(UUID id) {
        Supplier supplier = getSupplierById(id);
        if (supplier != null) {
            supplier.setDeleted(true);
            supplierRepository.save(supplier);
        }
    }
}
