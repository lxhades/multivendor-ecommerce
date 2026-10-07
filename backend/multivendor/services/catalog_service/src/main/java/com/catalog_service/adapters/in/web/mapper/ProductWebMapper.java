package com.catalog_service.adapters.in.web.mapper;
import com.catalog_service.application.command.CreateProductCommand;
import com.catalog_service.application.command.UpdateProductCommand;
import com.catalog_service.adapters.in.web.dto.request.CreateProductRequest;
import com.catalog_service.adapters.in.web.dto.request.UpdateProductRequest;
import org.springframework.stereotype.Component;
@Component
public class ProductWebMapper {
    public CreateProductCommand toCommand(CreateProductRequest r){return new CreateProductCommand(r.sellerId(),r.name(),r.description(),r.price());}
    public UpdateProductCommand toCommand(String id,UpdateProductRequest r){return new UpdateProductCommand(id,r.name(),r.description(),r.price(),r.status());}
}
