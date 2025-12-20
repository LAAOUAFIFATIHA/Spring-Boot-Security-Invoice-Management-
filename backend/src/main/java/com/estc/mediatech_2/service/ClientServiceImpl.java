package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dao.ClientDao;
import com.estc.mediatech_2.dto.ClientRequestDto;
import com.estc.mediatech_2.dto.ClientResponseDto;
import com.estc.mediatech_2.models.ClientEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientDao clientDao;

    @Override
    public ClientResponseDto save(ClientRequestDto request) {
        ClientEntity entity = new ClientEntity();
        entity.setNom_client(request.getNom_client());
        entity.setPrenom_client(request.getPrenom_client());
        entity.setTelephone(request.getTelephone());
        return mapToDto(clientDao.save(entity));
    }

    @Override
    public ClientResponseDto findById(Long id) {
        return clientDao.findById(id).map(this::mapToDto).orElse(null);
    }

    @Override
    public ClientResponseDto findByTel(String tel) {
        return clientDao.findByTelephone(tel).map(this::mapToDto).orElse(null);
    }

    @Override
    public ClientResponseDto update(ClientRequestDto request, Long id) {
        return clientDao.findById(id).map(entity -> {
            entity.setNom_client(request.getNom_client());
            entity.setPrenom_client(request.getPrenom_client());
            entity.setTelephone(request.getTelephone());
            return mapToDto(clientDao.save(entity));
        }).orElse(null);
    }

    @Override
    public void delete(Long id) {
        clientDao.deleteById(id);
    }

    @Override
    public List<ClientResponseDto> findAll() {
        return clientDao.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ClientResponseDto mapToDto(ClientEntity entity) {
        ClientResponseDto dto = new ClientResponseDto();
        dto.setId_client(entity.getId_client());
        dto.setNom_client(entity.getNom_client());
        dto.setPrenom_client(entity.getPrenom_client());
        dto.setTelephone(entity.getTelephone());
        return dto;
    }
}
