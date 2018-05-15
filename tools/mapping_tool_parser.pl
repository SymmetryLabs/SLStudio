#!/usr/bin/env perl
use strict;
use warnings;

my $origin_x = 24;
my $origin_z = -264;

while (my $x = <>) {
	my $z = <>;

	chomp ($x, $z);

	last if $x == $origin_x and $z == $origin_z;

	print "new TowerConfig($x, 0, $z, 0, 0, 0, new String[] { }),\n";
}
