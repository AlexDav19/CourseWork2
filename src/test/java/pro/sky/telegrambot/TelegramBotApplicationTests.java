package pro.sky.telegrambot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;

@SpringBootTest
class TelegramBotApplicationTests {

	@Autowired
	TelegramBotUpdatesListener telegramBotUpdatesListener;
	@Test
	void contextLoads() {
		Assertions.assertThat(telegramBotUpdatesListener).isNotNull();
	}

}
