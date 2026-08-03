package io.github.springwhale.test.json;

import io.github.springwhale.framework.core.json.SpringWhaleJsonProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import tools.jackson.databind.ObjectMapper;

import java.util.Locale;

@SpringBootTest
public abstract class BaseJacksonTest {

    protected final SpringWhaleJsonProperties jsonConfigBackup = new SpringWhaleJsonProperties();

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected SpringWhaleJsonProperties jsonProperties;

    private Locale originalDefaultLocale;
    private Locale originalContextLocale;

    @BeforeEach
    void setUp() {
        originalDefaultLocale = Locale.getDefault();
        originalContextLocale = LocaleContextHolder.getLocale();
        BeanUtils.copyProperties(jsonProperties, jsonConfigBackup);
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(originalDefaultLocale);
        LocaleContextHolder.setLocale(originalContextLocale);
        BeanUtils.copyProperties(jsonConfigBackup, jsonProperties);
    }

}