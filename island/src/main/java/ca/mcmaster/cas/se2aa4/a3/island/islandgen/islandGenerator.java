package ca.mcmaster.cas.se2aa4.a3.island.islandgen;

import java.io.IOException;
import ca.mcmaster.cas.se2aa4.a2.io.MeshFactory;
import ca.mcmaster.cas.se2aa4.a2.io.Structs;
import ca.mcmaster.cas.se2aa4.a3.island.shape.Circle;
import ca.mcmaster.cas.se2aa4.a3.island.shape.Square;
import ca.mcmaster.cas.se2aa4.a3.island.shape.TiltedOval;
import ca.mcmaster.cas.se2aa4.a3.island.shape.Shape;
import ca.mcmaster.cas.se2aa4.a3.island.Lagoon.Lagoon;
import ca.mcmaster.cas.se2aa4.a3.island.aquifers.Aquifers;
import ca.mcmaster.cas.se2aa4.a3.island.beaches.Beaches;
import ca.mcmaster.cas.se2aa4.a3.island.biomes.Biomes;
import ca.mcmaster.cas.se2aa4.a3.island.cities.Cities;
import ca.mcmaster.cas.se2aa4.a3.island.configuration.Configuration;
import ca.mcmaster.cas.se2aa4.a3.island.elevation.*;
import ca.mcmaster.cas.se2aa4.a3.island.lakes.Lakes;
import ca.mcmaster.cas.se2aa4.a3.island.lakes.Rivers;

public class islandGenerator {
    private Structs.Mesh aMesh;
    private Configuration config;
    public static String biomeArg;

    public islandGenerator(Configuration config) throws IOException {
        this.aMesh = new MeshFactory().read(config.input());
        this.config = config;
    }

    public Structs.Mesh generate() throws IOException {
        // biomeCond = (config.biomes() == null) ? false : true;
        biomeArg = (config.biomes());
        Structs.Mesh mesh = registerShape();
        if (config.mode() != null && config.mode().equals("lagoon"))
            mesh = new Lagoon(mesh).build(); 
        mesh = new Aquifers(mesh).enrichAquifers(config.aquifer()); 
        mesh = new Lakes(mesh).generateLakes(Integer.parseInt(config.lakes()));
        mesh = new Rivers(mesh).generateRivers(Integer.parseInt(config.rivers()));
        mesh = registerElevation(mesh);
        mesh = new Beaches(mesh).enrichBeaches();
        mesh = new Plains().addElevation(mesh);
        mesh = new Temp(mesh).enrichTemp(); 
        mesh = new Biomes(mesh).enrichBiomes();
        mesh = new Cities(mesh).generateCities(Integer.parseInt(config.cities()));

        // Seed generation
        SeedGen seedGen = new SeedGen();
        long seed = (config.seed() == null) ? (System.currentTimeMillis()) : Long.parseLong(config.seed());
        if (config.seed() == null)
            seedGen.saveMesh(seed, mesh);
        mesh = seedGen.getMesh(seed);
        System.out.println("SEED: " + seed);

        return mesh; // returns the mesh
    }

    public Structs.Mesh registerElevation(Structs.Mesh mesh) {
        if (config.elevation() == null)
            return mesh;

        switch (config.elevation()) {
            case "volcano":
                mesh = new Volcano().build(mesh);
                break;
            case "rockymountain":
                mesh = new RockMountain().build(mesh);
                break;
            case "both":
                mesh = new Volcano().build(mesh);
                mesh = new RockMountain().build(mesh);
                break;

            default:
                throw new IllegalArgumentException("Specified Elevation type does not exist -- " + config.elevation());
        }

        return mesh;
    }

    public Structs.Mesh registerShape() {
        Shape iMesh;
        MeshDimension dim = new MeshDimension(aMesh);

        // default shape if none is provided
        if (config.shape() == null)
            return new Circle(dim.maxX, dim.maxY).build(aMesh);

        switch (config.shape()) {
            case "square":
                iMesh = new Square(dim.maxX, dim.maxY);
                break;
            case "circle":
                iMesh = new Circle(dim.maxX, dim.maxY);
                break;
            case "oval":
                iMesh = new TiltedOval(dim.maxX, dim.maxY);
                break;
            default:
                throw new IllegalArgumentException("Unknown shape: " + config.shape());
        }
        return iMesh.build(aMesh);
    }
}