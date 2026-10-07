package com.catalog_service.application.usecase;
import com.catalog_service.application.command.CreateCategoryCommand;
import com.catalog_service.application.port.out.CategoryRepository;
import com.catalog_service.domain.model.aggregate.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class CreateCategoryUseCaseTest {
 @Mock CategoryRepository categoryRepository;
 @InjectMocks CreateCategoryUseCase useCase;
 @Test void createCategory_shouldCreateAndSaveCategory(){var command=new CreateCategoryCommand(null,"Điện thoại","dien-thoai");when(categoryRepository.existsBySlug("dien-thoai")).thenReturn(false);when(categoryRepository.save(any(Category.class))).thenAnswer(i->i.getArgument(0));var result=useCase.execute(command);assertNotNull(result);assertEquals("Điện thoại",result.name());assertEquals("dien-thoai",result.slug());verify(categoryRepository).save(any(Category.class));}
 @Test void createCategory_shouldRejectDuplicateSlug(){var command=new CreateCategoryCommand(null,"Điện thoại","dien-thoai");when(categoryRepository.existsBySlug("dien-thoai")).thenReturn(true);assertThrows(IllegalArgumentException.class,()->useCase.execute(command));verify(categoryRepository,never()).save(any(Category.class));}
 @Test void createChildCategory_shouldUseParent(){var parent=UUID.randomUUID();var command=new CreateCategoryCommand(parent.toString(),"iPhone","iphone");when(categoryRepository.existsBySlug("iphone")).thenReturn(false);when(categoryRepository.existsById(any())).thenReturn(true);when(categoryRepository.save(any(Category.class))).thenAnswer(i->i.getArgument(0));var result=useCase.execute(command);assertEquals(parent.toString(),result.parentId());}
}
