package com.ensias.healthcareapp;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Получение контекста приложения
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Проверка, что имя пакета совпадает с ожидаемым
        assertEquals("com.ensias.healthcareapp", appContext.getPackageName());
    }
}

