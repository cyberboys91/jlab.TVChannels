package jlab.tvchannels.model;

import java.util.ArrayList;

public class Country {
    private String name;
    private ArrayList<Community> communities;
    private int countChannels, countCommunities;

    public Country(String name, ArrayList<Community> communities, int countChannels) {
        this.name = name;
        this.communities = communities;
        this.countCommunities = communities.size();
        this.countChannels = countChannels <= 0 ? loadCountChannels(communities)
                : countChannels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Community> getCommunities() {
        return communities;
    }

    public int countChannels() {
        return countChannels;
    }

    public int countCommunities() {
        return countCommunities;
    }

    public void setCommunities(ArrayList<Community> communities) {
        this.communities = communities;
        countCommunities = communities.size();
        countChannels = loadCountChannels(communities);
    }

    private int loadCountChannels(ArrayList<Community> communities) {
        int result = 0;
        for (Community com : communities)
            result += com.getChannels().size();
        return result;
    }

    public void addCommunity(Community community) {
        this.communities.add(community);
        countCommunities++;
        countChannels += community.getChannels().size();
    }

    public void removeCommunity(Community community) {
        if(this.communities.remove(community)) {
            countCommunities--;
            countChannels -= community.getChannels().size();
        }
    }
}
