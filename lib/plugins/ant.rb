#!/usr/bin/env ruby

require File.dirname(__FILE__) + "/../pageobject.rb"

module Plugins
  class Ant < Jenkins::PageObject

    def self.add_ant_step(targets, ant_build_file)
      click_button 'Add build step'
      click_link 'Invoke Ant'
      fill_in '_.targets', :with => targets
    end

  end
end
