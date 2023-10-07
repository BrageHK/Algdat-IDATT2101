    import java.util.*;
    import java.io.*;

/*
 * Island interessepunkter:
 * - Øst: 10346	1	"Dalatangi"
 * - Vest: 102331	32	"Breiðavík"
 * - Sør: 94774	32	"Gardar on Reynisfjara Beach"
 * 
 * Skandinavia interessepunkter:
 * - Øst: 2618725	1	"Niemijärvi"
 * - Vest: 3722663	1	"Kalvåg"
 * - Nord: 3007953	1	"Nordkapp"
 * - Sør: 3539362	1	"Birkemose"
 */

class Vertex implements Comparable<Vertex> {

    final int value;
    int distance;
    Vertex link;

    public Vertex(int value, int distance) {
        this.value = value;
        this.distance = distance;
    }

    public Vertex(int value) {
        this(value, Integer.MAX_VALUE);
    }

    @Override
    public int compareTo(Vertex o) {
        if (distance < o.distance)
            return -1;
        else if (distance > o.distance)
            return 1;
        return 0;
    }
}

class Graph {

    private final int numberOfNodes;
    private final Vertex[] linkedVertecies;
    private final int[] distances;
    private final boolean[] visited;

    public Graph(int numberOfNodes, int numberOfPaths) {
        this.numberOfNodes = numberOfNodes;
        this.linkedVertecies = new Vertex[numberOfNodes];
        
    }

    public void addEdge(int from, int to, int weight) {

        Vertex currentVertex = linkedVertecies[from];
        if (currentVertex == null) {
            linkedVertecies[from] = new Vertex(to, weight);
            return;
        }
        while (currentVertex.link != null) {
            currentVertex = currentVertex.link;
        }
        currentVertex.link = new Vertex(to, weight);
    }

    public Vertex[] dijkstra(Vertex start, Vertex end) {
        // Sett distanser tio uendelig
        Vertex[] vertecies = new Vertex[numberOfNodes];
        start.distance = 0;


        for (int i = 0; i < numberOfNodes; i++)
            if (i != start.value)
                vertecies[i] = new Vertex(i);

        // Lag kø
        PriorityQueue<Vertex> queue = new PriorityQueue<>();
        queue.add(vertecies[start.value]);
        
        // Så lenge køen ikke er tom
        while (!queue.isEmpty()) {
            // Hent nærmest node
            Vertex vertex = queue.remove();

            Vertex neighbour = vertecies[linkedVertecies[vertex.value].value];

            // Beregn og eventuelt bytt distanse for hver node som er nabo
            while (neighbour != null) {              
                int old_cost = vertecies[neighbour.value].distance;
                int new_cost = vertecies[vertex.value].distance + neighbour.distance;

                // Bytt distanse om ny distanse er kortere
                if (new_cost < old_cost) {
                    queue.add(neighbour);
                    vertecies[neighbour.value].distance = new_cost;
                }

            neighbour = neighbour.link;
            }
        }
        queue.clear();
        return vertecies;
    }

    public Vertex[] getVertecies() {
        return linkedVertecies;
    }

    public boolean isLandmarkType(int categoryCode, String category) {
        switch (category) {
            case "Stedsnavn":
                return (categoryCode & 1) == 1;
            case "Bensinstasjon":
                return (categoryCode & 2) == 2;
            case "Ladestasjon":
                return (categoryCode & 4) == 4;
            case "Spisested":
                return (categoryCode & 8) == 8;
            case "Drikkested":
                return (categoryCode & 16) == 16;
            case "Overnattingssted":
                return (categoryCode & 32) == 32;
            default:
                return false;
        }
    }
}

class DijkstrasLandmarksApplication {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("List the landmarks you want. At least 2.");
            return;
        }
        int[] chosenLandmarks = Arrays.stream(args).mapToInt(s -> Integer.parseInt(s)).toArray();

        String nodesFile = "noder.txt"; // nodenr breddegrad lengdegrad
        String edgesFile = "kanter.txt"; // franode tilnode kjøretid lengde fartsgrense
        String landmarkFile = "interessepkt.txt"; // nodenr kode "Navn på stedet"

        String outputFile = "landemerker.txt";
        String reversedOutputFile = "reversed_landemerker.txt";

        BufferedReader nodesReader = new BufferedReader(new InputStreamReader(new FileInputStream(nodesFile)));
        BufferedReader edgeReader = new BufferedReader(new InputStreamReader(new FileInputStream(edgesFile)));
        BufferedReader landmarkReader = new BufferedReader(new InputStreamReader(new FileInputStream(landmarkFile)));

        StringTokenizer nodeST = new StringTokenizer(nodesReader.readLine());
        int nodes = Integer.parseInt(nodeST.nextToken());

        StringTokenizer edgeST = new StringTokenizer(edgeReader.readLine());
        int edges = Integer.parseInt(edgeST.nextToken());

        StringTokenizer landmarkST = new StringTokenizer(landmarkReader.readLine());
        int landmarks = Integer.parseInt(landmarkST.nextToken());

        // Create graphs with all nodes
        Graph graph = new Graph(nodes, edges);
        Graph reversedGraph = new Graph(nodes, edges);
        for (int i = 0; i < edges; i++) {
            edgeST = new StringTokenizer(edgeReader.readLine());
            int from = Integer.parseInt(edgeST.nextToken());
            int to = Integer.parseInt(edgeST.nextToken());
            int time = Integer.parseInt(edgeST.nextToken());

            graph.addEdge(from, to, time);
            reversedGraph.addEdge(to, from, time);
        }
        nodesReader.close();
        edgeReader.close();

        // Create table for landmark distances
        int[][] landmarkTimes = new int[nodes][chosenLandmarks.length];
        int[][] reversedLandmarkTimes = new int[nodes][chosenLandmarks.length];
        for (int i = 0; i < landmarks; i++) {
            landmarkST = new StringTokenizer(landmarkReader.readLine());
            int starting_vertex = Integer.parseInt(landmarkST.nextToken());

            for (int j = 0; j < chosenLandmarks.length; j++) {
                if (starting_vertex == chosenLandmarks[j]) {
                    System.out.println("At landmark " + starting_vertex + " of " + chosenLandmarks.length);

                    Vertex[] vertecies = graph.dijkstra(starting_vertex);
                    Vertex[] reversedVertecies = graph.dijkstra(starting_vertex);
                
                    for (int k = 0; k < vertecies.length; k++) {
                        landmarkTimes[k][j] = vertecies[k].distance;
                        reversedLandmarkTimes[k][j] = reversedVertecies[k].distance;
                    }
                }
            }
        }
        landmarkReader.close();

        StringBuilder sb = new StringBuilder();
        sb.append(chosenLandmarks[0]).append(" ").append(chosenLandmarks[1]).append(" ").append(chosenLandmarks[2]).append("\n");
        for (int i = 0; i < landmarkTimes.length; i++) {
            sb.append(i).append(" ");
            Arrays.stream(landmarkTimes[i]).forEach(n -> {
                sb.append(n + " ");
            });
            sb.append("\n");
        }

        try (BufferedWriter outpuWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile)));) {
            outpuWriter.write(sb.toString());
        } catch (IOException e) {
            System.out.println("IOException was caught");
        }

        StringBuilder sb2 = new StringBuilder();
        sb2.append(chosenLandmarks[0]).append(" ").append(chosenLandmarks[1]).append(" ").append(chosenLandmarks[2]).append("\n");
        for (int i = 0; i < reversedLandmarkTimes.length; i++) {
            sb2.append(i).append(" ");
            Arrays.stream(reversedLandmarkTimes[i]).forEach(n -> {
                sb2.append(n + " ");
            });
            sb2.append("\n");
        }

        try (BufferedWriter outpuWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(reversedOutputFile)));) {
            outpuWriter.write(sb2.toString());
        } catch (IOException e) {
            System.out.println("IOException was caught");
        }

    }
}
