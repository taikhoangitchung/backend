package app.service;

import app.entity.Type;
import app.repository.TypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeService {
    private final TypeRepository typeRepository;

    public void addType(String typeName) {
        if (typeRepository.existsByName(typeName)) {
            throw new IllegalArgumentException("Type already exists.");
        }
        Type type = new Type();
        type.setName(typeName);
        typeRepository.save(type);
    }
    public List<Type> getAllTypes() {
        return typeRepository.findAll();
    }
}
