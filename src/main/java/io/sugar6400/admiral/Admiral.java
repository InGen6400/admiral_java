package java.io.sugar6400.admiral;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class Admiral {
    private int shipPool, tankPool;
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

    public int DecideMove(Vector<int[]> energy_v, Hashtable<String, Ship> userTable){
        Mapping(energy_v, userTable);
        Pooling();

        INDArray shipFlattend = Nd4j.toFlattened(pooledShipMap);
        INDArray tankFlattend = Nd4j.toFlattened(pooledTankMap);

        INDArray inputData = Nd4j.hstack(shipFlattend, tankFlattend);

        INDArray output = model.output(inputData);

        int maxIdx = 0;
        for(int i = 0; i<output.length(); i++){
            if(output.getDouble(i) > output.getDouble(maxIdx)){
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    // TODO: XとYが逆転している可能性あり
    private void Mapping(Vector<int[]> energy_v, Hashtable<String, Ship> userTable){
        shipMap = Nd4j.zeros(256, 256);
        tankMap = Nd4j.zeros(256, 256);
        for(int[] energy: energy_v){
            tankMap.putScalar(new int[]{energy[0], energy[1]}, energy[2]);
        }
        for(Ship ship: userTable.values()){
            shipMap.putScalar(new int[]{ship.y, ship.x}, shipMap.getInt(ship.x, ship.y) + 1);
        }
    }

    private void Pooling(){
        pooledShipMap = Nd4j.zeros(256/shipPool, 256/shipPool);
        pooledTankMap = Nd4j.zeros(256/tankPool, 256/tankPool);
        // プーリング
        for(int y=0; y<256/shipPool; y++) {
            for (int x = 0; x < 256 / shipPool; x++) {
                int sum = 0;
                for(int dy=0; dy<shipPool; dy++) {
                    for (int dx = 0; dx < shipPool; dx++) {
                        sum += shipMap.getInt(y+dy, x+dx);
                    }
                }
                pooledShipMap.putScalar(new int[]{y, x}, sum);
            }
        }
        for(int y=0; y<256/tankPool; y++) {
            for (int x = 0; x < 256 / tankPool; x++) {
                int sum = 0;
                for(int dy=0; dy<tankPool; dy++) {
                    for (int dx = 0; dx < tankPool; dx++) {
                        sum += tankMap.getInt(y+dy, x+dx);
                    }
                }
                pooledTankMap.putScalar(new int[]{y, x}, sum);
            }
        }
    }
}
