package com.wow.libre.application.service.guild;

import com.wow.libre.domain.dto.GuildDto;
import com.wow.libre.domain.dto.GuildsDto;
import com.wow.libre.domain.model.CharacterDetail;
import com.wow.libre.domain.model.GuildBenefitModel;
import com.wow.libre.domain.model.GuildModel;
import com.wow.libre.domain.ports.in.characters.CharactersPort;
import com.wow.libre.domain.ports.in.guild.GuildPort;
import com.wow.libre.domain.ports.in.guild_benefits.GuildBenefitsPort;
import com.wow.libre.domain.ports.in.guild_member.GuildMemberPort;
import com.wow.libre.domain.ports.in.jwt.JwtPort;
import com.wow.libre.domain.ports.out.guild.ObtainGuild;
import com.wow.libre.infrastructure.entities.GuildEntity;
import com.wow.libre.infrastructure.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class GuildService implements GuildPort {
    private final ObtainGuild obtainGuild;
    private final GuildMemberPort guildMemberPort;
    private final CharactersPort charactersPort;
    private final GuildBenefitsPort guildBenefitsPort;
    private final JwtPort jwtPort;

    public GuildService(ObtainGuild obtainGuild, GuildMemberPort guildMemberPort, CharactersPort charactersPort,
                        GuildBenefitsPort guildBenefitsPort, JwtPort jwtPort) {
        this.obtainGuild = obtainGuild;
        this.guildMemberPort = guildMemberPort;
        this.charactersPort = charactersPort;
        this.guildBenefitsPort = guildBenefitsPort;
        this.jwtPort = jwtPort;
    }

    @Override
    public GuildsDto findAll(Integer size, Integer page, String transactionId) {

        GuildsDto guilds = new GuildsDto();
        List<GuildEntity> listGuilds = obtainGuild.getGuilds(size, page, transactionId);

        guilds.setGuilds(listGuilds.stream().map(this::mapToModel).toList());
        guilds.setSize(listGuilds.size());
        return guilds;
    }

    @Override
    public GuildDto detail(Long guildId, Long accountId, String authorizationHeader, String transactionId) {

        Integer accountWebId = jwtPort.extractAccountId(authorizationHeader);

        if (accountWebId != null) {

        }

        Optional<GuildModel> getGuild = obtainGuild.getGuild(guildId).map(this::mapToModel);

        if (getGuild.isEmpty()) {
            throw new NotFoundException("The requested guild does not exist", transactionId);
        }

        GuildModel guild = getGuild.get();

        List<GuildBenefitModel> benefits =
                guildBenefitsPort.getBenefits(guildId, transactionId).stream()
                        .filter(benefit -> new Date().before(benefit.expirationDate)).toList();

        return new GuildDto(guild.id, guild.name, guild.leaderName, guild.emblemStyle, guild.emblemColor,
                guild.borderStyle, guild.borderColor, guild.info, guild.motd, guild.createDate, guild.bankMoney,
                guild.members, guild.logo, guild.bannerPrimary, guild.bannerSecondary, benefits, null);
    }


    private GuildModel mapToModel(GuildEntity guildEntity) {
        Date dateCreate = Date.from(Instant.ofEpochMilli(guildEntity.getCreateDate() * 1000));
        long members = guildMemberPort.accountMemberGuildId(guildEntity.getId());
        CharacterDetail character = charactersPort.getCharacter(Long.valueOf(guildEntity.getLeaderGuid()), "");

        return new GuildModel(guildEntity.getId(), guildEntity.getName(), character.getName(),
                guildEntity.getEmblemStyle(), guildEntity.getEmblemColor(), guildEntity.getBorderStyle(),
                guildEntity.getBorderColor(), guildEntity.getInfo(), guildEntity.getMotd(),
                dateCreate, guildEntity.getBankMoney(), members, guildEntity.getLogo(),
                guildEntity.getBannerPrimary(), guildEntity.getBannerSecondary());
    }
}