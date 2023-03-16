package io.okandroid.bluetooth.le.service;

import java.util.UUID;

import io.okandroid.bluetooth.le.OkBleClient;

public class PulseGeneratorService extends AbstractService {
    public static final UUID PULSE_GENERATOR_SERVICE = UUID.fromString("0000face-0000-1000-8000-00805f9b34fb");


    public PulseGeneratorService(OkBleClient client) {
        super("Pulse Generator Service", client);
    }
}
