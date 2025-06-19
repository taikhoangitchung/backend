package app.service;

import app.dto.type.AddTypeRequest;
import app.entity.Type;
import app.exception.DuplicateException;
import app.repository.TypeRepository;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeService {
    private final TypeRepository typeRepository;
    private final MessageHelper messageHelper;

    public void addType(AddTypeRequest request) {
        if (typeRepository.existsByName(request.getName())) {
            throw new DuplicateException(messageHelper.get("type.exists"));
        }
        Type type = new Type();
        type.setName(request.getName());
        typeRepository.save(type);
    }

    public List<Type> getAllTypes() {
        return typeRepository.findAll();
    }
}
