package fr.usubelli.accounting.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.usubelli.accounting.common.VertxMicroService;
import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class OrganizationVertx implements VertxMicroService {

    private static final String APPLICATION_JSON = "application/json";

    private final MongoOrganizationRepository organizationRepository;
    private final ObjectMapper objectMapper;

    public OrganizationVertx(MongoOrganizationRepository organizationRepository, ObjectMapper objectMapper) {
        this.organizationRepository = organizationRepository;
        this.objectMapper = objectMapper;
    }

    public void route(Router router) {
        router.route().handler(BodyHandler.create());
        router.post("/organization")
                .produces(APPLICATION_JSON)
                .handler(createOrganization());
        router.get("/organization/:siren")
                .produces(APPLICATION_JSON)
                .handler(findOrganizationBySiren());
        router.put("/organization")
                .produces(APPLICATION_JSON)
                .handler(updateOrganization());
    }

    private Handler<RoutingContext> createOrganization() {
        return rc -> {
            try {
                rc.response().setStatusCode(200).end(
                        objectMapper.writeValueAsString(
                                organizationRepository.createOrganisation(
                                        objectMapper.readValue(rc.getBodyAsString(), Organisation.class))));
            } catch (OrganisationAlreadyExistsException e) {
                rc.response().setStatusCode(409).end();
            } catch (Exception e) {
                e.printStackTrace();
                rc.response().setStatusCode(500).end();
            }
        };
    }

    private Handler<RoutingContext> findOrganizationBySiren() {
        return rc -> {
            try {
                rc.response().setStatusCode(200).end(
                        objectMapper.writeValueAsString(
                                organizationRepository.findOrganisation(rc.request().getParam("siren"))));
            } catch (OrganisationNotFoundException e) {
                rc.response().setStatusCode(404).end();
            } catch (Exception e) {
                e.printStackTrace();
                rc.response().setStatusCode(500).end();
            }
        };
    }

    private Handler<RoutingContext> updateOrganization() {
        return rc -> {
            try {
                rc.response().setStatusCode(200).end(
                        objectMapper.writeValueAsString(
                                organizationRepository.updateOrganisation(
                                    objectMapper.readValue(rc.getBodyAsString(), Organisation.class))));
            } catch (OrganisationNotFoundException e) {
                rc.response().setStatusCode(404).end();
            } catch (Exception e) {
                e.printStackTrace();
                rc.response().setStatusCode(500).end();
            }
        };
    }

}
