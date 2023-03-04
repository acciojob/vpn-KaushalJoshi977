package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        User user = userRepository2.findById(userId).get();
        List<ServiceProvider> serviceProvider = user.getServiceProviderList();

        boolean canConnect = false;
        int id = 0;
        CountryName countryName1 = CountryName.valueOf(countryName);


        if (user == null) {
            throw new Exception("User not found");
        }
        if (user.getConnected()) {
            throw new Exception("Already connected");
        }
        if (user.getCountry().getCountryName().equals(countryName1)) {
            return user;
        }
        for (ServiceProvider s: serviceProvider) {
            for (Country c:  s.getCountryList()) {
                if(c.getCountryName() == countryName1){
                    canConnect = true;
                    id = s.getId();
                }
            }

        }
        if (canConnect == false) throw new Exception("Unable to connect");

        String maskedIp = countryName1.toCode() + "." + id + "." + userId;
        user.setMaskedIp(maskedIp);
        user.setConnected(true);

        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(serviceProviderRepository2.findById(id).get());
        List<Connection> connectionList = user.getConnectionList();
        connectionList.add(connection);
        user.setConnectionList(connectionList);

        userRepository2.save(user);
        return user;

    }
    @Override
    public User disconnect(int userId) throws Exception {
            User user = userRepository2.findById(userId).get();
            if(!user.getConnected()) throw new Exception("Already disconnected");
            user.setConnected(false);
            user.setMaskedIp(null);
            user.setConnectionList(null);
            userRepository2.save(user);
            return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).orElseThrow(() -> new Exception("Sender not found"));
        User receiver = userRepository2.findById(receiverId).orElseThrow(() -> new Exception("Receiver not found"));

        // Check if sender is already connected to the current country of the receiver
        CountryName receiverCurrentCountry = receiver.getCountry().getCountryName();
        if (sender.getCountry().getCountryName() == receiverCurrentCountry) {
            return sender;
        }

        // Check if the receiver is connected to a VPN, if yes then use that country for communication
        CountryName receiverConnectedCountry = receiver.getMaskedIp() != null ?
                CountryName.valueOf(receiver.getMaskedIp().split("\\.")[0]) : receiverCurrentCountry;

        // Find a suitable VPN for the sender to connect to in order to communicate with the receiver
        List<ServiceProvider> serviceProviders = sender.getServiceProviderList();
        ServiceProvider suitableProvider = null;
        for (ServiceProvider provider : serviceProviders) {
            if (provider.getCountryList().stream().anyMatch(country -> country.getCountryName() == receiverConnectedCountry)) {
                if (suitableProvider == null || suitableProvider.getId() > provider.getId()) {
                    suitableProvider = provider;
                }
            }
        }
        if (suitableProvider == null) {
            throw new Exception("Cannot establish communication");
        }

        // Connect sender to suitable VPN
        CountryName senderNewCountry = suitableProvider.getCountryList().stream()
                .filter(country -> country.getCountryName() == receiverCurrentCountry).findFirst()
                .orElseThrow(() -> new Exception("Cannot establish communication")).getCountryName();
        connect(senderId, senderNewCountry.toCode());

        return userRepository2.findById(senderId).get();
    }
}
