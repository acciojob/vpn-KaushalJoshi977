package com.driver.services.impl;

import com.driver.model.Admin;
import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    @Override
    public Admin register(String username, String password) {
        Admin admin = new Admin();
        admin.setPassword(password);
        admin.setUsername(username);
        adminRepository1.save(admin);
        return admin;
    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName) {
        Admin admin = adminRepository1.findById(adminId).get();
        int id = 0;
        for (ServiceProvider s: admin.getServiceProviders()) {
                if(s.getName() == providerName){
                    id = s.getId();
                }
        }
        ServiceProvider serviceProvider = serviceProviderRepository1.findById(id).get();
        List<ServiceProvider> serviceProviderList1 = admin.getServiceProviders();
        serviceProviderList1.add(serviceProvider);
        adminRepository1.save(admin);
        return admin;
    }

    @Override

    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception{
        ServiceProvider serviceProvider = serviceProviderRepository1.findById(serviceProviderId).get();

        Country country = new Country();
        CountryName countryName1 = CountryName.valueOf(countryName);
        country.setCountryName(countryName1);
        country.setCode(countryName1.toCode());

        List<Country> countryList = serviceProvider.getCountryList();
        countryList.add(country);

        serviceProviderRepository1.save(serviceProvider);
        return serviceProvider;
    }
}
