package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.dto.request.CreateAlphabetRequest;
import com.example.nihongo_app.dto.request.SubmitAlphabetPracticeRequest;
import com.example.nihongo_app.dto.response.AlphabetMatrixItemResponse;
import com.example.nihongo_app.dto.response.AlphabetMatrixGroupResponse;
import com.example.nihongo_app.dto.response.AlphabetPracticeResponse;
import com.example.nihongo_app.dto.response.AlphabetResponse;
import com.example.nihongo_app.dto.response.SubmitAlphabetPracticeResponse;
import com.example.nihongo_app.entity.Character;
import com.example.nihongo_app.entity.Character.CharacterType;
import com.example.nihongo_app.entity.Rank;
import com.example.nihongo_app.entity.User;
import com.example.nihongo_app.entity.UserCharacterProgress;
import com.example.nihongo_app.entity.UserExpLog;
import com.example.nihongo_app.repository.CharacterRepository;
import com.example.nihongo_app.repository.RankRepository;
import com.example.nihongo_app.repository.UserCharacterProgressRepository;
import com.example.nihongo_app.repository.UserExpLogRepository;
import com.example.nihongo_app.repository.UserRepository;
import com.example.nihongo_app.service.AlphabetService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AlphabetServiceImpl implements AlphabetService {

    private static final int PRACTICE_SIZE = 10;
    private static final int EXP_REWARD = 5;

    private final CharacterRepository characterRepository;
    private final UserCharacterProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final UserExpLogRepository expLogRepository;
    private final RankRepository rankRepository;

    @Override
    @Transactional
    public AlphabetResponse create(CreateAlphabetRequest request) {
        String symbol = request.getSymbol().trim();
        characterRepository.findByTypeAndSymbol(request.getType(), symbol).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Character already exists");
        });
        return toResponse(characterRepository.save(toEntity(request, symbol)));
    }

    @Override
    @Transactional
    public List<AlphabetResponse> createBulk(List<CreateAlphabetRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one character is required");
        }
        List<AlphabetResponse> saved = new ArrayList<>(requests.size());
        for (CreateAlphabetRequest request : requests) {
            saved.add(create(request));
        }
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlphabetMatrixGroupResponse> getMatrix(CharacterType type, Long userId) {
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type is required");
        }
        Map<String, List<AlphabetMatrixItemResponse>> grouped = new LinkedHashMap<>();
        characterRepository.findMatrixByTypeAndUserId(type.name(), userId).forEach(row ->
                grouped.computeIfAbsent(row.getGroupName(), ignored -> new ArrayList<>())
                        .add(AlphabetMatrixItemResponse.builder()
                                .characterId(row.getCharacterId())
                                .symbol(row.getSymbol())
                                .romaji(row.getRomaji())
                                .type(CharacterType.valueOf(row.getType()))
                                .audioUrl(row.getAudioUrl())
                                .strokeOrderData(row.getStrokeOrderData())
                                .orderIndex(row.getOrderIndex())
                                .masteryLevel(row.getMasteryLevel())
                                .build()));
        return grouped.entrySet().stream()
            .map(entry -> AlphabetMatrixGroupResponse.builder()
                .groupName(entry.getKey())
                .characters(entry.getValue())
                .build())
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlphabetPracticeResponse startPractice(Long userId) {
        List<Character> allCharacters = characterRepository.findAll();
        List<UserCharacterProgress> progresses = progressRepository.findAllByUserId(userId);
        Map<Long, UserCharacterProgress> progressByCharacter = progresses.stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.getCharacter().getId(), p -> p));

        List<Character> prioritizedCandidates = allCharacters.stream()
                .filter(character -> {
                    UserCharacterProgress progress = progressByCharacter.get(character.getId());
                    return progress == null || Objects.requireNonNullElse(progress.getMasteryLevel(), 0) < 3;
                })
                .sorted(Comparator
                        .comparing((Character character) -> {
                            UserCharacterProgress progress = progressByCharacter.get(character.getId());
                            return progress == null ? null : progress.getLastPracticedAt();
                        }, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(Character::getId))
                .toList();

                if (prioritizedCandidates.isEmpty()) {
            return AlphabetPracticeResponse.builder()
                .practiceSessionId(UUID.randomUUID().toString())
                .questions(List.of())
                .build();
        }
        if (allCharacters.size() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least four characters are required for multiple choice practice");
        }

        List<Character> selectionPool = new ArrayList<>(prioritizedCandidates.subList(0,
            Math.min(prioritizedCandidates.size(), PRACTICE_SIZE * 2)));
        Collections.shuffle(selectionPool);
        List<AlphabetPracticeResponse.Question> questions = selectionPool.stream()
            .limit(PRACTICE_SIZE)
                .map(character -> toQuestion(character, allCharacters))
                .toList();
        return AlphabetPracticeResponse.builder()
            .practiceSessionId(UUID.randomUUID().toString())
            .questions(questions)
            .build();
    }

    @Override
    @Transactional
    public SubmitAlphabetPracticeResponse submitPractice(Long userId,
                                                         SubmitAlphabetPracticeRequest request) {
        List<SubmitAlphabetPracticeRequest.Result> results = request == null ? null : request.getResults();
        if (results == null || results.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one result is required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        LocalDateTime practicedAt = LocalDateTime.now();
        for (SubmitAlphabetPracticeRequest.Result result : results) {
            if (result.getCharacterId() == null || result.getIsCorrect() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "characterId and isCorrect are required");
            }
            Character character = characterRepository.findById(result.getCharacterId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Character not found"));
            UserCharacterProgress progress = progressRepository
                    .findByUserIdAndCharacterId(userId, character.getId())
                    .orElseGet(() -> UserCharacterProgress.builder()
                            .user(user)
                            .character(character)
                            .masteryLevel(0)
                            .build());
            int current = Objects.requireNonNullElse(progress.getMasteryLevel(), 0);
            int next = Boolean.TRUE.equals(result.getIsCorrect()) ? current + 1 : current - 1;
            progress.setMasteryLevel(Math.max(0, Math.min(3, next)));
            progress.setLastPracticedAt(practicedAt);
            progressRepository.save(progress);
        }

        int currentExp = Objects.requireNonNullElse(user.getExp(), 0) + EXP_REWARD;
        user.setExp(currentExp);
        Rank qualifyingRank = rankRepository
                .findFirstByMinExpRequiredLessThanEqualOrderByOrderIndexDesc(currentExp)
                .orElse(null);
        boolean promoted = qualifyingRank != null
                && (user.getRank() == null || !Objects.equals(user.getRank().getId(), qualifyingRank.getId()));
        if (promoted) {
            user.setRank(qualifyingRank);
        }
        userRepository.save(user);
        expLogRepository.save(UserExpLog.builder()
                .userId(userId)
                .expGained(EXP_REWARD)
                .sourceType(UserExpLog.SourceType.ALPHABET_PRACTICE)
                .build());

        return SubmitAlphabetPracticeResponse.builder()
                .expEarned(EXP_REWARD)
                .currentExp(currentExp)
                .promoted(promoted)
                .newRankName(qualifyingRank == null ? null : qualifyingRank.getName())
                .message("Tuyệt vời! Bạn đã hoàn thành bài luyện tập.")
                .build();
    }

    private AlphabetPracticeResponse.Question toQuestion(Character character, List<Character> allCharacters) {
        boolean multipleChoice = ThreadLocalRandom.current().nextInt(100) < 70;
        List<AlphabetPracticeResponse.Option> options = List.of();
        if (multipleChoice) {
            List<Character> wrongAnswers = new ArrayList<>(allCharacters.stream()
                    .filter(candidate -> !Objects.equals(candidate.getId(), character.getId()))
                    .toList());
            Collections.shuffle(wrongAnswers);
            List<Character> selected = new ArrayList<>();
            selected.add(character);
            selected.addAll(wrongAnswers.subList(0, 3));
            Collections.shuffle(selected);
            options = selected.stream().map(option -> toOption(option, character)).toList();
        }
        return AlphabetPracticeResponse.Question.builder()
                .characterId(character.getId())
                .questionType(multipleChoice ? "MULTIPLE_CHOICE" : "DRAWING")
            .prompt(multipleChoice ? "Nghe va chon chu cai dung" : "Viet chu: " + character.getRomaji())
                .symbol(character.getSymbol())
                .romaji(character.getRomaji())
                .audioUrl(character.getAudioUrl())
                .strokeOrderData(multipleChoice ? null : character.getStrokeOrderData())
                .options(options)
                .build();
    }

    private AlphabetPracticeResponse.Option toOption(Character character, Character correctCharacter) {
        return AlphabetPracticeResponse.Option.builder()
                .optionId(character.getId())
                .content(character.getSymbol())
                .correct(Objects.equals(character.getId(), correctCharacter.getId()))
                .build();
    }

    private Character toEntity(CreateAlphabetRequest request, String symbol) {
        return Character.builder().symbol(symbol).romaji(request.getRomaji().trim())
                .type(request.getType()).groupName(request.getGroupName().trim())
                .audioUrl(request.getAudioUrl()).strokeOrderData(request.getStrokeOrderData())
                .orderIndex(request.getOrderIndex()).build();
    }

    private AlphabetResponse toResponse(Character character) {
        return AlphabetResponse.builder().id(character.getId()).symbol(character.getSymbol())
                .romaji(character.getRomaji()).type(character.getType()).groupName(character.getGroupName())
                .audioUrl(character.getAudioUrl()).strokeOrderData(character.getStrokeOrderData())
                .orderIndex(character.getOrderIndex()).build();
    }
}