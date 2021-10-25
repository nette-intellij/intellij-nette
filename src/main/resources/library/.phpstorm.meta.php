<?php

namespace PHPSTORM_META {

	use Nette\Application\UI\Presenter;
	use Nette\DI\Container;
	use Nette\Utils\Arrays;

	override(
		Container::getService(0),
		map( [
			'application' => \Nette\Application\Application::class,
		])
	);

	override(
		Container::getByType(0),
		map([
			'' => '@',
		])
	);

	override(Arrays::get(0), elementType(0));
	override(Arrays::getRef(0), elementType(0));
	override(Arrays::grep(0), type(0));

	exitPoint(Presenter::terminate());
	exitPoint(Presenter::sendResponse());
	exitPoint(Presenter::sendJson());
	exitPoint(Presenter::sendTemplate());
	exitPoint(Presenter::sendPayload());
	exitPoint(Presenter::forward());
	exitPoint(Presenter::redirect());
	exitPoint(Presenter::redirectUrl());
	exitPoint(Presenter::restoreRequest());
}