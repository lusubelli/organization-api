package fr.usubelli.accounting.organization;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoOrganizationRepository {


    private final MongoConfig mongoConfig;
    private MongoCollection<Organisation> organizationCollection;

    public MongoOrganizationRepository(MongoConfig mongoConfig) {
        this.mongoConfig = mongoConfig;
    }

    private MongoCollection<Organisation> organisationCollection() {
        if (organizationCollection == null) {
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            final MongoClient mongoClient = new MongoClient(mongoConfig.getHost(), new MongoClientOptions.Builder().connectTimeout(1000).serverSelectionTimeout(1000).build());
            final MongoDatabase database = mongoClient.getDatabase(mongoConfig.getDatabase());
            final MongoCollection<Organisation> organization = database.getCollection(mongoConfig.getCollection(), Organisation.class);
            organizationCollection = organization.withCodecRegistry(pojoCodecRegistry);
        }
        return organizationCollection;
    }

    public final Organisation createOrganisation(final Organisation organisation) throws OrganisationAlreadyExistsException {
        try {
            findOrganisation(organisation.getSiren());
            throw new OrganisationAlreadyExistsException();
        } catch (OrganisationNotFoundException e) {
            System.out.println(String.format("Create organisation siren [%s]", organisation.getSiren()));
            organisationCollection().insertOne(organisation);
            Organisation o2 = organisation;
            try {
                o2 = findOrganisation(organisation.getSiren());
                System.out.println(String.format("Created organisation siren [%s] : \n %s", organisation.getSiren(), o2));
            } catch (OrganisationNotFoundException ex) {
                // TODO log
                ex.printStackTrace();
            }
            return o2;
        }
    }

    public final Organisation updateOrganisation(final Organisation organisation) throws OrganisationNotFoundException {
        if(findOrganisation(organisation.getSiren()) == null) {
            throw new OrganisationNotFoundException();
        }
        System.out.println(String.format("Update organisation siren [%s]", organisation.getSiren()));
        organisationCollection().replaceOne(eq("siren", organisation.getSiren()), organisation);
        System.out.println(String.format("Updated organisation siren [%s] : \n %s", organisation.getSiren(),
                findOrganisation(organisation.getSiren())));
        return findOrganisation(organisation.getSiren());
    }

    public final Organisation findOrganisation(final String siren) throws OrganisationNotFoundException {
        System.out.println(String.format("Find organisation by siren [%s]", siren));
        final Organisation organisation = organisationCollection().find(eq("siren", siren)).first();
        System.out.println(String.format("Organisation by siren [%s] : \n %s", siren, organisation));
        if (organisation == null) {
            throw new OrganisationNotFoundException();
        }
        return organisation;
    }

}
