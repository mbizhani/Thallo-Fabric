package org.devocative.thallo.fabric.gateway.iservice;

import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;

import java.io.IOException;
import java.security.cert.CertificateException;

public interface IFabricCAService {
	Wallet enroll(String username, String password) throws InvalidArgumentException, EnrollmentException, CertificateException, IOException;

	boolean register(String username, String password, String type, String registerUsername) throws Exception;

	boolean isUserInWallet(String username);
}
