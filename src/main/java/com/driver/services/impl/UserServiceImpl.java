package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{
        User user = new User();
        user.setPassword(password);
        user.setUsername(username);

        Country country = new Country();
        CountryName countryName1 = CountryName.valueOf(countryName);
        country.setCountryName(countryName1);
        country.setCode(countryName1.toCode());

        user.setCountry(country);
        user.setOriginalCountry(countryName);
        user.setConnected(false);
        user.setMaskedIp(null);
        String originalIp = user.getId() + "." + countryName1.toCode();
        user.setOriginalIp(originalIp);

        userRepository3.save(user);

        return user;

    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        User user = userRepository3.findById(userId).get();
        List<ServiceProvider> serviceProviderList1 = user.getServiceProviderList();
        serviceProviderList1.add(serviceProviderRepository3.findById(serviceProviderId).get());
        userRepository3.save(user);
        return user;
    }
}
