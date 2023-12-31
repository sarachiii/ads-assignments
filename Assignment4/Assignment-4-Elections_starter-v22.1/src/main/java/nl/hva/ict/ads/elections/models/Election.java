package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import nl.hva.ict.ads.utils.xml.XMLParser;

import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds all election data per consituency
 * Provides calculation methods for overall election results
 */
public class Election {

    private String name;

    // all (unique) parties in this election, organised by Id
    // will be build from the XML
    protected Map<Integer, Party> parties;

    // all (unique) constituencies in this election, identified by Id
    protected Set<Constituency> constituencies;

    public Election(String name) {
        this.name = name;
        this.parties = new TreeMap<>(Integer::compareTo);
        this.constituencies = new TreeSet<>(Comparator.comparing(Constituency::getId));
    }

    /**
     * finds all (unique) parties registered for this election
     *
     * @return all parties participating in at least one constituency, without duplicates
     */
    public Collection<Party> getParties() {
        return this.parties.values().stream().toList();
    }

    /**
     * finds the party with a given Id
     *
     * @param Id
     * @return the party with given Id, or null if no such party exists.
     */
    public Party getParty(int Id) {
        return this.parties.get(Id);
    }

    public Set<? extends Constituency> getConstituencies() {
        return this.constituencies;
    }

    /**
     * finds all unique candidates across all parties across all constituencies
     * organised by increasing party-id and then by increasing candidate id.
     *
     * @return alle unique candidates organised in an ordered set.
     */
    public List<Candidate> getAllCandidates() {
        return this.parties.values().stream().flatMap(party -> party.getCandidates().stream()).toList();
    }

    /**
     * Retrieve for the given party the number of Candidates that have been registered per Constituency
     *
     * @param party
     * @return
     */
    public Map<Constituency, Integer> numberOfRegistrationsByConstituency(Party party) {

        return this.constituencies.stream().collect(Collectors.toMap(constituency -> constituency,
                constituency -> constituency.getCandidates(party).size(), Integer::sum));
    }

    /**
     * Finds all Candidates that have a duplicate name against another candidate in the election
     * (can be in the same party or in another party)
     *
     * @return
     */
    public Set<Candidate> getCandidatesWithDuplicateNames() {
        Set<Candidate> candidates = this.parties.values().stream().flatMap(party ->
                party.getCandidates().stream()).collect(Collectors.toSet());

        List<String> fullNames = candidates.stream().map(Candidate::getFullName).toList();

        return candidates.stream().filter(candidate ->
                Collections.frequency(fullNames, candidate.getFullName()) > 1).collect(Collectors.toSet());
    }

    /**
     * Retrieve from all constituencies the combined sub set of all polling stations that are located within the area of the specified zip codes
     * i.e. firstZipCode <= pollingStation.zipCode <= lastZipCode
     * All valid zip codes adhere to the pattern 'nnnnXX' with 1000 <= nnnn <= 9999 and 'AA' <= XX <= 'ZZ'
     *
     * @param firstZipCode
     * @param lastZipCode
     * @return the sub set of polling stations within the specified zipCode range
     */
    public Collection<PollingStation> getPollingStationsByZipCodeRange(String firstZipCode, String lastZipCode) {
        return this.constituencies.stream().flatMap(constituency -> constituency.getPollingStationsByZipCodeRange(firstZipCode, lastZipCode).stream()).collect(Collectors.toSet());
    }

    /**
     * Retrieves per party the total number of votes across all candidates, constituencies and polling stations
     *
     * @return
     */
    public Map<Party, Integer> getVotesByParty() {
        return this.constituencies.stream().flatMap(constituency -> constituency.getVotesByParty()
                .entrySet().stream()).collect(Collectors.toMap(partyIntegerEntry -> partyIntegerEntry.getKey(), partyIntegerEntry -> partyIntegerEntry.getValue(),Integer::sum));
    }

    /**
     * Retrieves per party the total number of votes across all candidates,
     * that were cast in one out of the given collection of polling stations.
     * This method is useful to prepare an election result for any sub-area of a Constituency.
     * Or to obtain statistics of special types of voting, e.g. by mail.
     *
     * @param pollingStations the polling stations that cover the sub-area of interest
     * @return
     */
    public Map<Party, Integer> getVotesByPartyAcrossPollingStations(Collection<PollingStation> pollingStations) {

        if (pollingStations == null) return null;

        return this.constituencies.stream().flatMap(constituency -> constituency.getPollingStations().stream()
                        .filter(pollingStation -> Collections.frequency(pollingStations.stream().toList(), pollingStation) > 0))
                .collect(Collectors.toSet()).stream().flatMap(pollingStation -> pollingStation.getVotesByParty().entrySet()
                        .stream()).collect(Collectors.toMap(partyIntegerEntry -> partyIntegerEntry.getKey(),
                        partyIntegerEntry -> partyIntegerEntry.getValue(), Integer::sum));
    }

    /**
     * Transforms and sorts decreasingly vote counts by party into votes percentages by party
     * The party with the highest vote count shall be ranked upfront
     * The votes percentage by party is calculated from  100.0 * partyVotes / totalVotes;
     *
     * @return the sorted list of (party,votesPercentage) pairs with the highest percentage upfront
     */
    public static List<Map.Entry<Party, Double>> sortedElectionResultsByPartyPercentage(int tops, Map<Party, Integer> votesCounts) {

        int totalVotes = integersSum(votesCounts.values());

        List<Map.Entry<Party, Double>> percentages = new ArrayList<>(votesCounts.keySet().stream().collect(Collectors.toMap(party -> party,
                party -> (Double.valueOf(votesCounts.get(party))/totalVotes*100))).entrySet().stream().toList());

        percentages.sort((o1, o2) -> {
            if (o1.getValue() > o2.getValue()) return -1;
            if (o1.getValue() < o2.getValue()) return 1;
            return 0;
        });

        return percentages.subList(0, tops);
    }

    /**
     * Find the most representative Polling Station, which has got its votes distribution across all parties
     * the most alike the distribution of overall total votes.
     * A perfect match is found, if for each party the percentage of votes won at the polling station
     * is identical to the percentage of votes won by the party overall in the election.
     * The most representative Polling Station has the smallest deviation from that perfect match.
     * <p>
     * There are different metrics possible to calculate a relative deviation between distributions.
     * You may use the helper method {@link #euclidianVotesDistributionDeviation(Map, Map)}
     * which calculates a relative least-squares deviation between two distributions.
     *
     * @return the most representative polling station.
     */
    public PollingStation findMostRepresentativePollingStation() {
        Map<Party,Integer> totalVotesByParty = this.getVotesByParty();
        Set<PollingStation> pollingStations = this.constituencies.stream().flatMap(constituency ->
                constituency.getPollingStations().stream()).collect(Collectors.toSet());

        Map<Party, Integer> votesByPartyByStation = pollingStations.stream().flatMap(pollingStation ->
                pollingStation.getVotesByParty().entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,Integer::sum));
        Map<PollingStation,Double> deviations = new HashMap<>();

        for (PollingStation p : pollingStations){
            deviations.put(p,this.euclidianVotesDistributionDeviation(totalVotesByParty,votesByPartyByStation));
        }


        return Collections.max(deviations.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
    }

    /**
     * Calculates the Euclidian distance between the relative distribution across parties of two voteCounts.
     * If the two relative distributions across parties are identical, then the distance will be zero
     * If some parties have relatively more votes in one distribution than the other, the outcome will be positive.
     * The lower the outcome, the more alike are the relative distributions of the voteCounts.
     * ratign of votesCounts1 relative to votesCounts2.
     * see https://towardsdatascience.com/9-distance-measures-in-data-science-918109d069fa
     *
     * @param votesCounts1 one distribution of votes across parties.
     * @param votesCounts2 another distribution of votes across parties.
     * @return de relative distance between the two distributions.
     */
    private double euclidianVotesDistributionDeviation(Map<Party, Integer> votesCounts1, Map<Party, Integer> votesCounts2) {
        // calculate total number of votes in both distributions
        int totalNumberOfVotes1 = integersSum(votesCounts1.values());
        int totalNumberOfVotes2 = integersSum(votesCounts2.values());

        // we calculate the distance as the sum of squares of relative voteCount distribution differences per party
        // if we compare two voteCounts that have the same relative distribution across parties, the outcome will be zero

        return votesCounts1.entrySet().stream()
                .mapToDouble(e -> Math.pow(e.getValue() / (double) totalNumberOfVotes1 -
                        votesCounts2.getOrDefault(e.getKey(), 0) / (double) totalNumberOfVotes2, 2))
                .sum();
    }

    /**
     * auxiliary method to calculate the total sum of a collection of integers
     *
     * @param integers
     * @return
     */
    public static int integersSum(Collection<Integer> integers) {
        return integers.stream().reduce(Integer::sum).orElse(0);
    }


    public String prepareSummary(int partyId) {

        Party party = this.getParty(partyId);
        StringBuilder summary = new StringBuilder()
                .append("\nSummary of ").append(party).append(":\n");
        summary.append("\nTotal number of candidates = ").append(party.getCandidates().size());
        summary.append("\nCandidates: ");

        // List of all candidates with line breaks after every 5 parties
        int end = 5;
        for (int i = 0; i <= party.getCandidates().size(); i += 5) {
            summary.append(party.getCandidates().stream().toList().subList(i, end)).append("\n");

            if (end + 5 < party.getCandidates().size()) {
                end += 5;
            } else {
                end = party.getCandidates().size();
            }

            if (i + 5 > end) {
                break;
            }
        }
        summary.append("\nTotal number of registrations = ").append(numberOfRegistrationsByConstituency(party).values().stream().mapToInt(Integer::intValue).sum());
        summary.append("\nNumber of registrations per constituency: ").append(numberOfRegistrationsByConstituency(party));
        return summary.toString();
    }

    public String prepareSummary() {

        StringBuilder summary = new StringBuilder()
                .append("\nElection summary of ").append(this.name).append(":\n\n");
        summary.append(getParties().size()).append(" Participating parties:\n");

        // List of all parties with line breaks after every 5 parties
        int end = 5;
        for (int i = 0; i <= getParties().size(); i += 5) {
            summary.append(getParties().stream().toList().subList(i, end)).append("\n");

            if (end + 5 < getParties().size()) {
                end += 5;
            } else {
                end = getParties().size();
            }

            if (i + 5 > end) {
                break;
            }
        }

        summary.append("\nTotal number of constituencies = " + getConstituencies().size());

        int pollingStations = 0;
        for (Constituency c : this.constituencies) {
            pollingStations += c.getPollingStations().size();
        }
        summary.append("\nTotal number of polling stations = " + pollingStations);
        summary.append("\nTotal number of candidates in the election = " + getAllCandidates().size());
        summary.append("\nDifferent candidates with duplicate names across different parties are:\n").append(getCandidatesWithDuplicateNames());
        summary.append("\n\nOverall election results by party percentage:\n");

        // A list of the election results by party percentage
        List<Map.Entry<Party, Double>> electionList = sortedElectionResultsByPartyPercentage(this.getVotesByParty().size(), this.getVotesByParty());
        // List of all election results with line breaks after every 3 results
        end = 3;
        for (int i = 0; i <= electionList.size(); i += 3) {

            if (!electionList.subList(i, end).isEmpty()) {
                summary.append(electionList.subList(i, end)).append("\n");
            }

            if (end + 3 < electionList.size()) {
                end += 3;
            } else {
                end = electionList.size();
            }

            if (i + 3 > end) {
                break;
            }
        }

        Collection<PollingStation> pollingStationsByZipCodeRange = getPollingStationsByZipCodeRange("1091AA", "1091ZZ");
        summary.append("\n\nPolling stations in Amsterdam Wibautstraat area with zip codes 1091AA-1091ZZ:\n").append(pollingStationsByZipCodeRange);
        summary.append("\n\nTop 10 election results by party percentage in Amsterdam area with zip codes 1091AA-1091ZZ:\n");
        // Map with votes of parties by zip code range
        Map<Party, Integer> votesByParties = getVotesByPartyAcrossPollingStations(pollingStationsByZipCodeRange);

        // List of top ten elected parties
        int topNumber = 10;
        List<Map.Entry<Party, Double>> topTenElections = sortedElectionResultsByPartyPercentage(topNumber, votesByParties);

        // List of top ten parties with line breaks after every two parties
        end = 2;
        for (int i = 0; i <= topTenElections.size(); i += 2) {

            if (!topTenElections.subList(i, end).isEmpty()) {
                summary.append(topTenElections.subList(i, end)).append("\n");
            }

            if (end + 2 < topTenElections.size()) {
                end += 2;
            } else {
                end = topTenElections.size();
            }

            if (i + 2 > end) {
                break;
            }
        }
        summary.append("\n\nMost representative polling station is:\n").append(findMostRepresentativePollingStation());

        // TODO report the sorted election results by decreasing party percentage of the most representative polling station
        summary.append("\n\nMost representative polling station is:\n").append(this.sortedElectionResultsByPartyPercentage(
                findMostRepresentativePollingStation().getVotesByParty().size(),findMostRepresentativePollingStation().getVotesByParty()));

        return summary.toString();
    }

    /**
     * Reads all data of Parties, Candidates, Contingencies and PollingStations from available files in the given folder and its subfolders
     * This method can cope with any structure of sub folders, but does assume the file names to comply with the conventions
     * as found from downloading the files from https://data.overheid.nl/dataset/verkiezingsuitslag-tweede-kamer-2021
     * So, you can merge folders after unpacking the zip distributions of the data, but do not change file names.
     *
     * @param folderName the root folder with the data files of the election results
     * @return een Election met alle daarbij behorende gegevens.
     * @throws XMLStreamException bij fouten in een van de XML bestanden.
     * @throws IOException        als er iets mis gaat bij het lezen van een van de bestanden.
     */
    public static Election importFromDataFolder(String folderName) throws XMLStreamException, IOException {
        System.out.println("Loading election data from " + folderName);
        Election election = new Election(folderName);
        int progress = 0;
        Map<Integer, Constituency> kieskringen = new HashMap<>();
        for (Path constituencyCandidatesFile : PathUtils.findFilesToScan(folderName, "Kandidatenlijsten_TK2021_")) {
            XMLParser parser = new XMLParser(new FileInputStream(constituencyCandidatesFile.toString()));
            Constituency constituency = Constituency.importFromXML(parser, election.parties);
            //election.constituenciesM.put(constituency.getId(), constituency);
            election.constituencies.add(constituency);
            showProgress(++progress);
        }
        System.out.println();
        progress = 0;
        for (Path votesPerPollingStationFile : PathUtils.findFilesToScan(folderName, "Telling_TK2021_gemeente")) {
            XMLParser parser = new XMLParser(new FileInputStream(votesPerPollingStationFile.toString()));
            election.importVotesFromXml(parser);
            showProgress(++progress);
        }
        System.out.println();
        return election;
    }

    protected static void showProgress(final int progress) {
        System.out.print('.');
        if (progress % 50 == 0) System.out.println();
    }

    /**
     * Auxiliary method for parsing the data from the EML files
     * This methode can be used as-is and does not require your investigation or extension.
     */
    public void importVotesFromXml(XMLParser parser) throws XMLStreamException {
        if (parser.findBeginTag(Constituency.CONSTITUENCY)) {

            int constituencyId = 0;
            if (parser.findBeginTag(Constituency.CONSTITUENCY_IDENTIFIER)) {
                constituencyId = parser.getIntegerAttributeValue(null, Constituency.ID, 0);
                parser.findAndAcceptEndTag(Constituency.CONSTITUENCY_IDENTIFIER);
            }

            //Constituency constituency = this.constituenciesM.get(constituencyId);
            final int finalConstituencyId = constituencyId;
            Constituency constituency = this.constituencies.stream()
                    .filter(c -> c.getId() == finalConstituencyId)
                    .findFirst()
                    .orElse(null);

            //parser.findBeginTag(PollingStation.POLLING_STATION_VOTES);
            while (parser.findBeginTag(PollingStation.POLLING_STATION_VOTES)) {
                PollingStation pollingStation = PollingStation.importFromXml(parser, constituency, this.parties);
                if (pollingStation != null) constituency.add(pollingStation);
            }

            parser.findAndAcceptEndTag(Constituency.CONSTITUENCY);
        }
    }

    /**
     * HINTS:
     * getCandidatesWithDuplicateNames:
     *  Approach-1: first build a Map that counts the number of candidates per given name
     *              then build the collection from all candidates, excluding those whose name occurs only once.
     *  Approach-2: build a stream that is sorted by name
     *              apply a mapMulti that drops unique names but keeps the duplicates
     *              this approach probably requires complex lambda expressions that are difficult to justify
     */

}
