package org.example.Service.Service;

import org.example.Common.Exception.ApiException;
import org.example.Service.DTO.CreateServiceRequest;
import org.example.Service.DTO.ServiceResponse;
import org.example.Service.DTO.UpdateServiceRequest;
import org.example.Service.Repository.ServiceRepository;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceManagementService {
    private final ServiceRepository serviceRepository;
    private final SpecialistRepository specialistRepository;

    public ServiceManagementService(ServiceRepository serviceRepository,
                                    SpecialistRepository specialistRepository){
        this.serviceRepository = serviceRepository;
        this.specialistRepository = specialistRepository;
    }

    @Transactional
    public ServiceResponse createService(UUID specialistId, CreateServiceRequest request){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        org.example.Service.Entity.Service service = org.example.Service.Entity.Service.builder().serviceName(request.serviceName())
                .description(request.description())
                .price(request.price())
                .duration(request.duration())
                .specialist(specialist).build();

        serviceRepository.save(service);
        return toServiceResponse(service);
    }

    public ServiceResponse getServiceById(UUID serviceId){
        org.example.Service.Entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        return  toServiceResponse(service);
    }

    public List<ServiceResponse> getAllServiceBySpecialist(UUID specialistId){
        List<org.example.Service.Entity.Service> services = serviceRepository.findAllServiceBySpecialist_Id(specialistId);

        return services.stream()
                .map(this::toServiceResponse)
                .toList();
    }

    public List<ServiceResponse> getAllServiceBySlug(String slug){
        Specialist specialist = specialistRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        List<org.example.Service.Entity.Service> services = serviceRepository.findAllServiceBySpecialist_Id(specialist.getId());

        return services.stream()
                .map(this::toServiceResponse)
                .toList();
    }

    @Transactional
    public ServiceResponse updateService(UUID serviceId, UUID specialistId, UpdateServiceRequest request){
        org.example.Service.Entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ApiException("Service not found",HttpStatus.NOT_FOUND));

        if(service.getSpecialist().getId() != specialistId)
            throw new ApiException("Service can be edited only by its creator",HttpStatus.FORBIDDEN);

        if(request.serviceName() != null) service.setServiceName(request.serviceName());
        if(request.description() != null) service.setDescription(request.description());
        if(request.price() != null) service.setPrice(request.price());
        if(request.duration() != null) service.setDuration(request.duration());

        serviceRepository.save(service);
        return toServiceResponse(service);
    }

    @Transactional
    public void changeActiveStatus(UUID serviceId){
        org.example.Service.Entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ApiException("Service not found",HttpStatus.NOT_FOUND));
        service.setActive(!service.isActive());
        serviceRepository.save(service);
    }

    private ServiceResponse toServiceResponse(org.example.Service.Entity.Service service){
        return new ServiceResponse(
                service.getId(),
                service.getSpecialist().getId(),
                service.getServiceName(),
                service.getDescription(),
                service.getPrice(),
                service.getDuration(),
                service.isActive(),
                service.getCreatedAt()
        );
    }
}