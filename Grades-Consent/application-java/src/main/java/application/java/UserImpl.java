package application.java;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.security.PrivateKey;
import java.util.Set;

public class UserImpl implements User {
    private String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private Enrollment enrollment;
    private String mspId;
    X509Identity adminIdentity;

    public UserImpl(String name, String affiliation, X509Identity adminIdentity, String mspId) {
        this.name = name;
        this.affiliation = affiliation;
        this.adminIdentity = adminIdentity;
        this.mspId = mspId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public String getAffiliation() {
        return affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return new Enrollment() {

            @Override
            public PrivateKey getKey() {
                return adminIdentity.getPrivateKey();
            }

            @Override
            public String getCert() {
                return Identities.toPemString(adminIdentity.getCertificate());
            }
        };
    }

    @Override
    public String getMspId() {
        return mspId;
    }
}
