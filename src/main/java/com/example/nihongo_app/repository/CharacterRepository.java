package com.example.nihongo_app.repository;

import com.example.nihongo_app.entity.Character;
import com.example.nihongo_app.entity.Character.CharacterType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CharacterRepository extends JpaRepository<Character, Long> {

    List<Character> findAllByTypeOrderByGroupNameAscOrderIndexAscIdAsc(CharacterType type);

    Optional<Character> findByTypeAndSymbol(CharacterType type, String symbol);

        @Query(value = """
                             SELECT c.id AS characterId, c.symbol AS symbol, c.romaji AS romaji,
                                 c.type AS type, c.group_name AS groupName, c.audio_url AS audioUrl,
                                     c.stroke_order_data AS strokeOrderData, c.order_index AS orderIndex,
                                     COALESCE(ucp.mastery_level, 0) AS masteryLevel
                        FROM characters c
                        LEFT JOIN user_character_progress ucp
                            ON ucp.character_id = c.id AND ucp.user_id = :userId
                        WHERE c.type = :type
                        ORDER BY c.group_name, c.order_index, c.id
                        """, nativeQuery = true)
        List<AlphabetMatrixProjection> findMatrixByTypeAndUserId(@Param("type") String type,
                                                                                                                            @Param("userId") Long userId);

        interface AlphabetMatrixProjection {
                Long getCharacterId();
                String getSymbol();
                String getRomaji();
                String getType();
                String getGroupName();
                String getAudioUrl();
                String getStrokeOrderData();
                Integer getOrderIndex();
                Integer getMasteryLevel();
        }
}