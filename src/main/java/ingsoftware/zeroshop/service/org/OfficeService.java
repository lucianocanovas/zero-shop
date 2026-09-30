package ingsoftware.zeroshop.service.org;

import ingsoftware.zeroshop.dto.OfficeFormDTO;
import ingsoftware.zeroshop.entity.actor.Contact;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.enums.ContactType;
import ingsoftware.zeroshop.enums.OfficeType;
import ingsoftware.zeroshop.enums.PhoneType;
import ingsoftware.zeroshop.repository.location.CityRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OfficeService {

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private CityRepository cityRepository;

    public List<Office> getAllOffices() {
        return officeRepository.findAllByDeletedFalse();
    }

    public Office getOfficeById(UUID id) {
        return officeRepository.findActive(id).orElse(null);
    }

    public Office createOffice(OfficeFormDTO dto) {
        Office office = new Office();
        office.setName(dto.getName());
        office.setCuit(dto.getCuit());
        office.setType(OfficeType.valueOf(dto.getType()));

        office.setContact(new java.util.ArrayList<>());
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) {
            ContactPhone phone = new ContactPhone();
            phone.setContactType(ContactType.WORK);
            phone.setPhoneType(PhoneType.MOBILE);
            phone.setPhoneNumber(dto.getPhone());
            phone.setObservation("Teléfono Sucursal");
            office.getContact().add(phone);
        }

        office.setAddress(new java.util.ArrayList<>());
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
                office.getAddress().add(address);
            }
        }

        return officeRepository.save(office);
    }

    public Office updateOffice(UUID id, OfficeFormDTO dto) {
        Office office = getOfficeById(id);
        if (office == null) return null;

        office.setName(dto.getName());
        office.setCuit(dto.getCuit());
        office.setType(OfficeType.valueOf(dto.getType()));

        if (office.getContact() == null) {
            office.setContact(new java.util.ArrayList<>());
        }
        if (dto.getPhone() != null && !dto.getPhone().isEmpty() && office.getContact().isEmpty()) {
            ContactPhone phone = new ContactPhone();
            phone.setContactType(ContactType.WORK);
            phone.setPhoneType(PhoneType.MOBILE);
            phone.setPhoneNumber(dto.getPhone());
            phone.setObservation("Teléfono Sucursal");
            office.getContact().add(phone);
        }

        if (office.getAddress() == null) {
            office.setAddress(new java.util.ArrayList<>());
        }
        if (dto.getAddress() != null && dto.getAddress().getCityId() != null && office.getAddress().isEmpty()) {
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
                office.getAddress().add(address);
            }
        }

        return officeRepository.save(office);
    }

    public void addAddressToOffice(UUID officeId, Address address) {
        Office office = getOfficeById(officeId);
        if (office != null) {
            if (office.getAddress() == null) {
                office.setAddress(new java.util.ArrayList<>());
            }
            office.getAddress().add(address);
            officeRepository.save(office);
        }
    }

    public void removeAddressFromOffice(UUID officeId, UUID addressId) {
        Office office = getOfficeById(officeId);
        if (office != null && office.getAddress() != null) {
            for (Address a : office.getAddress()) {
                if (a.getId() != null && a.getId().equals(addressId)) {
                    a.setDeleted(true);
                    break;
                }
            }
            officeRepository.save(office);
        }
    }

    public void addContactToOffice(UUID officeId, Contact contact) {
        Office office = getOfficeById(officeId);
        if (office != null) {
            if (office.getContact() == null) {
                office.setContact(new java.util.ArrayList<>());
            }
            office.getContact().add(contact);
            officeRepository.save(office);
        }
    }

    public void removeContactFromOffice(UUID officeId, UUID contactId) {
        Office office = getOfficeById(officeId);
        if (office != null && office.getContact() != null) {
            for (Contact c : office.getContact()) {
                if (c.getId() != null && c.getId().equals(contactId)) {
                    c.setDeleted(true);
                    break;
                }
            }
            officeRepository.save(office);
        }
    }

    public void deleteOffice(UUID id) {
        Office office = getOfficeById(officeId(id));
        if (office != null) {
            office.setDeleted(true);
            officeRepository.save(office);
        }
    }

    private UUID officeId(UUID id) { return id; }
}

