package app.service;

import app.dto.AddTypeRequest;
import app.entity.Type;
import app.repository.TypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeService {
    private final TypeRepository typeRepository;

    public void addType(AddTypeRequest request) {
        if (typeRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Type already exists.");
        }
        Type type = new Type();
        type.setName(request.getName());
        typeRepository.save(type);
    }
    public List<Type> getAllTypes() {
        return typeRepository.findAll();
    }
}
