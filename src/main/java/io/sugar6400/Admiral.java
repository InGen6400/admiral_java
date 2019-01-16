package io.sugar6400;

import org.apache.commons.lang3.ObjectUtils;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.KerasSequentialModel;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class Admiral {
    private int shipPool=4, tankPool=16;
    private MultiLayerNetwork model;
    private INDArray shipMap;
    private INDArray tankMap;
    private INDArray pooledShipMap;
    private INDArray pooledTankMap;

    public Admiral(){
        // モデル読み込み
        try {
            String simpleMlp = new ClassPathResource("model.h5").getFile().getPath();
            model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
        } catch (UnsupportedKerasConfigurationException | IOException | InvalidKerasConfigurationException e) {
            e.printStackTrace();
        }
    }

    private int roll(int x){
        if (x >  128) x -= 256;
        if (x < -128) x += 256;
        return x;
    }

    public int DecideMove(Vector<int[]> energy_v, Hashtable<String, Ship> userTable, int dx, int dy) {
        Mapping(energy_v, userTable);
        /*
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                if (tankMap.getInt(y, x) != 0) {
                    System.out.println(x + "," + y + " : " + tankMap.getInt(y, x));
                }
            }
        }*/
        INDArray tmpShip = shipMap.dup();
        INDArray tmpTank = tankMap.dup();

        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                int[] shifted = new int[]{roll(y + 128 - dy), roll(x + 128 - dx)};
                shipMap.putScalar(shifted, tmpShip.getInt(y, x));
                tankMap.putScalar(shifted, tmpTank.getInt(y, x));
            }
        }
        Pooling();

        INDArray shipFlattened = Nd4j.toFlattened(pooledShipMap);
        INDArray tankFlattened = Nd4j.toFlattened(pooledTankMap);

        INDArray inputData = Nd4j.concat(0, shipFlattened, tankFlattened);
        inputData = inputData.reshape(new int[]{1, (int) inputData.length()});
        INDArray output = model.output(inputData);
        return Nd4j.argMax(output).getInt(0);
    }

    private void Mapping(Vector<int[]> energy_v, Hashtable<String, Ship> userTable){
        shipMap = Nd4j.zeros(256, 256);
        tankMap = Nd4j.zeros(256, 256);
        for(int[] energy: energy_v){
            tankMap.putScalar(new int[]{energy[1], energy[0]}, tankMap.getInt(energy[1], energy[0]) + energy[2]);
        }
        for(Ship ship: userTable.values()){
            shipMap.putScalar(new int[]{ship.y, ship.x}, shipMap.getInt(ship.y, ship.x) + 1);
        }
    }

    private void Pooling(){
        pooledShipMap = Nd4j.zeros(256/shipPool, 256/shipPool);
        pooledTankMap = Nd4j.zeros(256/tankPool, 256/tankPool);
        // プーリング
        for(int y=0; y<256; y+=shipPool) {
            for (int x = 0; x<256; x+=shipPool) {
                int sum = 0;
                for(int dy=0; dy<shipPool; dy++) {
                    for (int dx = 0; dx < shipPool; dx++) {
                        sum += shipMap.getInt(y+dy, x+dx);
                    }
                }
                pooledShipMap.putScalar(new int[]{y/shipPool, x/shipPool}, sum);
            }
        }
        for(int y=0; y < 256; y+=tankPool) {
            for (int x = 0; x < 256; x+=tankPool) {
                int sum = 0;
                for(int dy=0; dy<tankPool; dy++) {
                    for (int dx = 0; dx < tankPool; dx++) {
                        sum += tankMap.getInt(y+dy, x+dx);
                    }
                }
                pooledTankMap.putScalar(new int[]{y/tankPool, x/tankPool}, sum);
            }
        }
    }
}
