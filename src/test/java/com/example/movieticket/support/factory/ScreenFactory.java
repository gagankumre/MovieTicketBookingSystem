package com.example.movieticket.support.factory;

import com.example.movieticket.domain.Screen;
import com.example.movieticket.domain.Theater;

public final class ScreenFactory {

    private ScreenFactory() {
    }

    public static Screen screen(Theater theater, String name) {
        return new Screen(theater, name);
    }

    public static Screen withId(Long id, Screen screen) {
        screen.setId(id);
        return screen;
    }
}
