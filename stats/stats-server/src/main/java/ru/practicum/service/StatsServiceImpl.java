package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitCreateDto;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.model.mapper.StatDtoMapper;
import ru.practicum.repository.StatsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public void saveHit(HitCreateDto createDto) {
        statsRepository.save(StatDtoMapper.mapToModel(createDto));
    }

    @Override
    public List<ResponseStatsDto> getStats(RequestStatsDto request) {
        List<String> requestUris;
        if (request.getUris() == null || request.getUris().isEmpty()) {
            requestUris = null;
        } else {
            requestUris = request.getUris();
        }

        List<ResponseStatsDto> response;
        if (request.getUnique()) {
            response = statsRepository.findUniqueStats(request.getStart(), request.getEnd(), requestUris);
        } else {
            response = statsRepository.findStats(request.getStart(), request.getEnd(), requestUris);
        }

        return response;
    }
}
