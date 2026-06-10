package com.example.movilidadmdq.config;

import com.example.movilidadmdq.exception.ApiError;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig
{

    @Bean
    public OpenAPI movilidadMDQOpenAPI()
    {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("API MovilidadMDQ")
                        .description("Servicio para la comparación de precios de transporte en Mar del Plata. Ingrese el JWT obtenido en /usuarios/login para ejecutar peticiones protegidas.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Soporte MovilidadMDQ")
                                .email("soporte@movilidadmdq.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    // Customizer global de OpenAPI para los errores de la API.
    //
    // Sin esto, las anotaciones @ApiResponse(responseCode = "400", description...)
    // en los controllers no le dicen a Swagger que schema usar en el ejemplo,
    // y springdoc cae al schema del 200 (por ejemplo AuthResponse) como
    // fallback. Eso es confuso: el ejemplo del error muestra campos del
    // exito.
    //
    // Este bean recorre toda la spec OpenAPI ya generada y, para cada
    // response con codigo 4xx o 5xx, asigna el schema de ApiError como
    // contenido application/json. Resultado: Swagger muestra el shape real
    // del error (timestamp, status, error, message, path, errores) en
    // todos los endpoints, sin tocar una sola anotacion @ApiResponse.

    @Bean
    public OpenApiCustomizer errorResponseSchemaCustomizer()
    {
        return openApi ->
        {
            // 1. Registrar ApiError como component schema si todavia no esta.
            //    (No esta porque ningun controller lo declara explicitamente.)
            ResolvedSchema resolved = ModelConverters.getInstance()
                    .resolveAsResolvedSchema(new AnnotatedType(ApiError.class));

            if (openApi.getComponents() == null)
            {
                openApi.setComponents(new Components());
            }
            if (openApi.getComponents().getSchemas() == null
                    || !openApi.getComponents().getSchemas().containsKey("ApiError"))
            {
                openApi.getComponents().addSchemas("ApiError", resolved.schema);
                resolved.referencedSchemas.forEach((nombre, schemaRef) ->
                        openApi.getComponents().addSchemas(nombre, schemaRef));
            }

            // 2. Apuntar las responses de error al schema recien registrado.
            Schema<?> apiErrorRef = new Schema<>().$ref("#/components/schemas/ApiError");

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation ->
                    {
                        if (operation.getResponses() == null) return;
                        operation.getResponses().forEach((codigo, response) ->
                        {
                            if (codigo.startsWith("4") || codigo.startsWith("5"))
                            {
                                response.content(new Content().addMediaType(
                                        "application/json",
                                        new MediaType().schema(apiErrorRef)
                                ));
                            }
                        });
                    })
            );
        };
    }
}
